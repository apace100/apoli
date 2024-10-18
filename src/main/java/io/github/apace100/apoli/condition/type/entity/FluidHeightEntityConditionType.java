package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.access.SubmergableEntity;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;

public class FluidHeightEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<FluidHeightEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("fluid", SerializableDataTypes.FLUID_TAG)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.DOUBLE),
        data -> new FluidHeightEntityConditionType(
            data.get("fluid"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("fluid", conditionType.fluidTag)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final TagKey<Fluid> fluidTag;

    private final Comparison comparison;
    private final double compareTo;

    public FluidHeightEntityConditionType(TagKey<Fluid> fluidTag, Comparison comparison, double compareTo) {
        this.fluidTag = fluidTag;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(Entity entity) {
        return comparison.compare(((SubmergableEntity) entity).apoli$getFluidHeightLoosely(fluidTag), compareTo);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.FLUID_HEIGHT;
    }

}
