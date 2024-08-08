package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;

public class WalkOnFluidPower extends Power {

    private final TagKey<Fluid> fluidTag;

    public WalkOnFluidPower(PowerType type, LivingEntity entity, TagKey<Fluid> fluidTag) {
        super(type, entity);
        this.fluidTag = fluidTag;
    }

    public TagKey<Fluid> getFluidTag() {
        return fluidTag;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("walk_on_fluid"),
            new SerializableData()
                .add("fluid", SerializableDataTypes.FLUID_TAG),
            data ->
                (type, player) -> new WalkOnFluidPower(type, player, data.get("fluid")))
            .allowCondition();
    }
}
