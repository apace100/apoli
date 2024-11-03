package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class WalkOnFluidPowerType extends PowerType {

    public static final TypedDataObjectFactory<WalkOnFluidPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("fluid", SerializableDataTypes.FLUID_TAG),
        (data, condition) -> new WalkOnFluidPowerType(
            data.get("fluid"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("fluid", powerType.getFluidTag())
    );

    private final TagKey<Fluid> fluidTag;

    public WalkOnFluidPowerType(TagKey<Fluid> fluidTag, Optional<EntityCondition> condition) {
        super(condition);
        this.fluidTag = fluidTag;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.WALK_ON_FLUID;
    }

    public TagKey<Fluid> getFluidTag() {
        return fluidTag;
    }

}
