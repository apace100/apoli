package io.github.apace100.apoli.global;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.*;
import io.github.apace100.calio.Calio;
import io.github.apace100.calio.data.CompoundSerializableDataType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.TagLike;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class GlobalPowerSet implements Comparable<GlobalPowerSet> {

    public static final CompoundSerializableDataType<GlobalPowerSet> DATA_TYPE = SerializableDataType.compound(
        new SerializableData()
            .add("entity_types", SerializableDataType.optional(SerializableDataTypes.ENTITY_TYPE_TAG_LIKE, false), Optional.empty())
            .add("powers", ApoliDataTypes.POWER_REFERENCE.list())
            .add("replace", SerializableDataTypes.BOOLEAN, false)
            .add("order", SerializableDataTypes.INT, 0),
        data -> new GlobalPowerSet(
            data.get("entity_types"),
            data.get("powers"),
            data.get("replace"),
            data.get("order")
        ),
        (globalPowerSet, serializableData) -> serializableData.instance()
            .set("entity_types", globalPowerSet.getEntityTypes())
            .set("powers", globalPowerSet.getPowerReferences())
            .set("replace", globalPowerSet.shouldReplace())
            .set("order", globalPowerSet.getOrder())
    );

    private final Optional<TagLike<EntityType<?>>> entityTypes;

    private final List<PowerReference> powerReferences;
    private final Set<Power> powers;

    private final boolean replace;
    private final int order;

    public GlobalPowerSet(Optional<TagLike<EntityType<?>>> entityTypes, Collection<PowerReference> powerReferences, boolean replace, int order) {
        this.entityTypes = entityTypes;
        this.powerReferences = new ObjectArrayList<>(powerReferences);
        this.powers = getSelfAndSubPowers(powerReferences);
        this.replace = replace;
        this.order = order;
    }

    @Override
    public int compareTo(@NotNull GlobalPowerSet that) {
        return Integer.compare(this.order, that.order);
    }

    private List<PowerReference> getPowerReferences() {
        return powerReferences;
    }

    private Set<Power> getSelfAndSubPowers(Collection<PowerReference> powerReferences) {

        Set<Power> result = new ObjectLinkedOpenHashSet<>();

        for (PowerReference powerReference : powerReferences) {

            //  Add the reference as is to the resulting set
            result.add(powerReference);

            //  If the reference is referring to a multiple power, query its sub-powers and add those to the
            //  resulting set
			powerReference.getOptionalReference()
				.filter(MultiplePower.class::isInstance)
				.map(MultiplePower.class::cast)
                .map(MultiplePower::getSubPowers)
                .ifPresent(result::addAll);

		}

        return result;

    }

    public GlobalPowerSet merge(GlobalPowerSet that) {

        RegistryKey<Registry<EntityType<?>>> key = RegistryKeys.ENTITY_TYPE;
        DynamicRegistryManager.Immutable drm = Calio.getDynamicRegistries().orElseThrow(() -> new IllegalStateException("Cannot merge global power set without dynamic registries!"));

        TagLike.Builder<EntityType<?>> thisBuilder = new TagLike.Builder<>(key);
        TagLike.Builder<EntityType<?>> thatBuilder = new TagLike.Builder<>(key);

        this.getEntityTypes().map(TagLike::entries).ifPresent(thisBuilder::addAll);
        that.getEntityTypes().map(TagLike::entries).ifPresent(thatBuilder::addAll);

        Set<PowerReference> powerReferences = new ObjectLinkedOpenHashSet<>(this.getPowerReferences());
        int order = this.getOrder();

        if (that.shouldReplace()) {

            thisBuilder.clear();
            powerReferences.clear();

            order = that.getOrder();

        }

        thisBuilder.addAll(thatBuilder);
        powerReferences.addAll(that.getPowerReferences());

        Optional<TagLike<EntityType<?>>> builtEntityTypes = this.getEntityTypes().isPresent() || that.getEntityTypes().isPresent()
            ? Optional.of(thisBuilder.build(drm.getWrapperOrThrow(key)))
            : Optional.empty();

        return new GlobalPowerSet(
            builtEntityTypes,
            powerReferences,
            that.shouldReplace(),
            order
        );

    }

    public boolean doesApply(EntityType<?> entityType) {
        return entityTypes.map(types -> types.contains(entityType)).orElse(true);
    }

    public boolean doesApply(Entity entity) {
        return this.doesApply(entity.getType());
    }

    public Optional<TagLike<EntityType<?>>> getEntityTypes() {
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
            .filter(p -> !PowerManager.contains(p.getId()))
            .collect(Collectors.toList());

        powers.removeIf(invalid::contains);
        invalid.removeIf(pt -> PowerManager.isDisabled(pt.getId()));

        return invalid;

    }

}
