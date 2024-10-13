package io.github.apace100.apoli.condition.type.fluid;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.FluidConditionType;
import io.github.apace100.apoli.condition.type.FluidConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;

public class InTagFluidConditionType extends FluidConditionType {

    public static final DataObjectFactory<InTagFluidConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("tag", SerializableDataTypes.FLUID_TAG),
        data -> new InTagFluidConditionType(
            data.get("tag")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("tag", conditionType.tag)
    );

    private final TagKey<Fluid> tag;

    public InTagFluidConditionType(TagKey<Fluid> tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(FluidState fluidState) {
        return fluidState.isIn(tag);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return FluidConditionTypes.IN_TAG;
    }

}
