package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;

public class WalkOnFluidPowerType extends PowerType {

    private final TagKey<Fluid> fluidTag;

    public WalkOnFluidPowerType(Power power, LivingEntity entity, TagKey<Fluid> fluidTag) {
        super(power, entity);
        this.fluidTag = fluidTag;
    }

    public TagKey<Fluid> getFluidTag() {
        return fluidTag;
    }

    public static PowerTypeFactory<WalkOnFluidPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("walk_on_fluid"),
            new SerializableData()
                .add("fluid", SerializableDataTypes.FLUID_TAG),
            data -> (power, entity) -> new WalkOnFluidPowerType(power, entity,
                data.get("fluid")
            )
        ).allowCondition();
    }

}
