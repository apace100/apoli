package io.github.apace100.apoli.global;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.MultiplePower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.calio.Calio;
import io.github.apace100.calio.data.CompoundSerializableDataType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.TagLike;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class GlobalPowerSet implements Comparable<GlobalPowerSet> {

    public static final CompoundSerializableDataType<GlobalPowerSet> DATA_TYPE = SerializableDataType.compound(
        new SerializableData()
            .add("entity_types", SerializableDataTypes.ENTITY_TYPE_TAG_LIKE, null)
            .add("powers", ApoliDataTypes.POWER_REFERENCE.listOf())
            .add("replace", SerializableDataTypes.BOOLEAN, false)
            .add("order", SerializableDataTypes.INT, 0),
        data -> new GlobalPowerSet(
            data.get("entity_types"),
            data.get("powers"),
            data.get("replace"),
            data.get("order")
        ),
        (globalPowerSet, data) -> data
            .set("entity_types", globalPowerSet.getEntityTypes())
            .set("powers", globalPowerSet.getPowers())
            .set("replace", globalPowerSet.shouldReplace())
            .set("order", globalPowerSet.getOrder())
    );

    @Nullable
    private final TagLike<EntityType<?>> entityTypes;
    private final Set<Power> powers;

    private final boolean replace;
    private final int order;

    public GlobalPowerSet(@Nullable TagLike<EntityType<?>> entityTypes, Collection<Power> powers, boolean replace, int order) {
        this.entityTypes = entityTypes;
        this.powers = getSelvesAndSubs(powers);
        this.replace = replace;
        this.order = order;
    }

    @Override
    public int compareTo(@NotNull GlobalPowerSet o) {
        return Integer.compare(order, o.order);
    }

    private Set<Power> getSelvesAndSubs(Collection<Power> powers) {

        Set<Power> result = new LinkedHashSet<>();
        for (Power power : powers) {

            //  Add the power to the result list
            result.add(power);

            //  Check whether the power is an instance of a multiple power
            MultiplePower multiplePower = getMultiple(power);
            if (multiplePower != null) {
                //  Add the sub-powers of the multiple power to the result list
                multiplePower.getSubPowers()
                    .stream()
                    .filter(PowerManager::contains)
                    .map(PowerManager::get)
                    .forEach(result::add);
            }

        }

        return result;

    }

    @Nullable
    private MultiplePower getMultiple(Power power) {

        if (power instanceof MultiplePower multiplePowerType) {
            return multiplePowerType;
        }

        else if (power instanceof PowerReference powerTypeRef && powerTypeRef.getReference() instanceof MultiplePower multiplePowerType) {
            return multiplePowerType;
        }

        else {
            return null;
        }

    }

    public GlobalPowerSet merge(GlobalPowerSet that) {

        RegistryKey<Registry<EntityType<?>>> key = RegistryKeys.ENTITY_TYPE;
        DynamicRegistryManager.Immutable drm = Calio
            .getDynamicRegistries()
            .orElseThrow(() -> new IllegalStateException("Cannot merge global power set without dynamic registries!"));

        TagLike.Builder<EntityType<?>> thisBuilder = new TagLike.Builder<>(key);
        TagLike.Builder<EntityType<?>> thatBuilder = new TagLike.Builder<>(key);

        List<Power> powers = new LinkedList<>(this.powers);

        if (this.getEntityTypes() != null) {
            thisBuilder.addAll(this.getEntityTypes().entries());
        }

        if (that.getEntityTypes() != null) {
            thatBuilder.addAll(that.getEntityTypes().entries());
        }

        if (that.shouldReplace()) {
            thisBuilder.clear();
            powers.clear();
        }

        thisBuilder.addAll(thatBuilder);
        powers.addAll(that.getPowers());

        return new GlobalPowerSet(
            thisBuilder.build(drm.getWrapperOrThrow(key)),
            powers,
            that.shouldReplace(),
            that.order
        );

    }

    public boolean doesApply(EntityType<?> entityType) {
        return entityTypes == null || entityTypes.contains(entityType);
    }

    public boolean doesApply(Entity entity) {
        return this.doesApply(entity.getType());
    }

    @Nullable
    public TagLike<EntityType<?>> getEntityTypes() {
        return entityTypes;
    }

    public ImmutableList<Power> getPowers() {
        return ImmutableList.copyOf(powers);
    }

    public boolean shouldReplace() {
        return replace;
    }

    public int getOrder() {
        return order;
    }

    /**
     *  Checks the defined power references of this global power set whether they all exist and have been loaded correctly.
     *  Invalid powers will be removed from this global power set as a side effect.
     *
     *  @return a {@link List} of {@link Power Powers} that were removed from this global power set.
     */
    public List<Power> validate() {

        List<Power> invalid = powers
            .stream()
            .filter(pt -> !PowerManager.contains(pt.getId()))
            .collect(Collectors.toList());

        invalid.forEach(powers::remove);
        invalid.removeIf(pt -> PowerManager.isDisabled(pt.getId()));

        return invalid;

    }

}
