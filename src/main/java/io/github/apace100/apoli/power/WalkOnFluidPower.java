package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.Tag;

public class WalkOnFluidPower extends Power {

    private final Tag<Fluid> fluidTag;

    public WalkOnFluidPower(PowerType<?> type, LivingEntity entity, Tag<Fluid> fluidTag) {
        super(type, entity);
        this.fluidTag = fluidTag;
    }

    public Tag<Fluid> getFluidTag() {
        return fluidTag;
    }
}
