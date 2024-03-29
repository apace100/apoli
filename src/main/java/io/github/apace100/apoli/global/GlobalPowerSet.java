package io.github.apace100.apoli.global;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.MultiplePowerType;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObject;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.util.TagLike;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class GlobalPowerSet implements Comparable<GlobalPowerSet>, DataObject<GlobalPowerSet> {

    private final TagLike<EntityType<?>> entityTypes;
    private final List<PowerType<?>> powerTypes;

    private final boolean replace;
    private final int loadingPriority;

    private int order;

    public GlobalPowerSet(int order, TagLike<EntityType<?>> entityTypes, List<PowerType<?>> powerTypes, boolean replace, int loadingPriority) {
        this.entityTypes = entityTypes;
        this.powerTypes = getSubPowers(powerTypes);
        this.replace = replace;
        this.order = order;
        this.loadingPriority = loadingPriority;
    }

    private List<PowerType<?>> getSubPowers(List<PowerType<?>> powerTypes) {

        List<PowerType<?>> result = new LinkedList<>();
        for (PowerType<?> powerType : powerTypes) {

            //  Add the power to the result list
            result.add(powerType);

            //  Check whether the power is an instance of a multiple power
            MultiplePowerType<?> multiplePowerType = getMultiplePowerType(powerType);
            if (multiplePowerType == null) {
                continue;
            }

            //  Add the sub-powers of the multiple power to the result list
            multiplePowerType.getSubPowers()
                .stream()
                .filter(PowerTypeRegistry::contains)
                .map(PowerTypeRegistry::get)
                .forEach(result::add);

        }

        return result;

    }

    @Nullable
    private MultiplePowerType<?> getMultiplePowerType(PowerType<?> powerType) {

        if (powerType instanceof PowerTypeReference<?> powerTypeRef && powerTypeRef.getReferencedPowerType() instanceof MultiplePowerType<?> multiplePowerType) {
            return multiplePowerType;
        }

        if (powerType instanceof MultiplePowerType<?> multiplePowerType) {
            return multiplePowerType;
        }

        return null;

    }

    public void merge(GlobalPowerSet otherSet) {

        this.order = otherSet.order;

        if (otherSet.shouldReplace()) {
            this.entityTypes.clear();
            this.powerTypes.clear();
        }

        if (otherSet.getEntityTypes() != null) {
            this.entityTypes.addAll(otherSet.getEntityTypes());
        }

        if (otherSet.getPowerTypes() != null) {
            this.powerTypes.addAll(otherSet.getPowerTypes());
        }

    }

    public boolean doesApply(EntityType<?> entityType) {
        return entityTypes == null || entityTypes.contains(entityType);
    }

    public boolean doesApply(Entity entity) {
        return doesApply(entity.getType());
    }

    public TagLike<EntityType<?>> getEntityTypes() {
        return entityTypes;
    }

    public List<PowerType<?>> getPowerTypes() {
        return powerTypes;
    }

    public int getLoadingPriority() {
        return loadingPriority;
    }

    public int getOrder() {
        return order;
    }

    public boolean shouldReplace() {
        return replace;
    }

    @Override
    public int compareTo(@NotNull GlobalPowerSet o) {
        return Integer.compare(order, o.order);
    }

    /**
     * Checks the defined power type references of this set to validate
     * that they all exist and have been loaded correctly.
     * Invalid power types will be removed from this set as a side effect.
     * @return List containing all invalid power types that were removed from the set
     */
    public List<PowerType<?>> validate() {

        List<PowerType<?>> invalid = powerTypes
            .stream()
            .filter(pt -> !PowerTypeRegistry.contains(pt.getIdentifier()))
            .collect(Collectors.toList());
        powerTypes.removeAll(invalid);

        invalid.removeIf(pt -> PowerTypeRegistry.isDisabled(pt.getIdentifier()));
        return invalid;

    }

    @Override
    public DataObjectFactory<GlobalPowerSet> getFactory() {
        return FACTORY;
    }

    public static final SerializableData DATA = new SerializableData()
        .add("replace", SerializableDataTypes.BOOLEAN, false)
        .add("entity_types", SerializableDataTypes.ENTITY_TYPE_TAG_LIKE, null)
        .add("powers", SerializableDataType.list(ApoliDataTypes.POWER_TYPE))
        .add("order", SerializableDataTypes.INT, 0)
        .add("loading_priority", SerializableDataTypes.INT, 0);

    public static final DataObjectFactory<GlobalPowerSet> FACTORY = new Factory();

    private static class Factory implements DataObjectFactory<GlobalPowerSet> {

        @Override
        public SerializableData getData() {
            return DATA;
        }

        @Override
        public GlobalPowerSet fromData(SerializableData.Instance data) {
            return new GlobalPowerSet(
                data.get("order"),
                data.get("entity_types"),
                data.get("powers"),
                data.get("replace"),
                data.get("loading_priority")
            );
        }

        @Override
        public SerializableData.Instance toData(GlobalPowerSet globalPowerSet) {

            SerializableData.Instance data = DATA.new Instance();

            data.set("order", globalPowerSet.getOrder());
            data.set("entity_types", globalPowerSet.getEntityTypes());
            data.set("powers", globalPowerSet.getPowerTypes());
            data.set("replace", globalPowerSet.shouldReplace());
            data.set("loading_priority", globalPowerSet.getLoadingPriority());

            return data;

        }

    }

}
