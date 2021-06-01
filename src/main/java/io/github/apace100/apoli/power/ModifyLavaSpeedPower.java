package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ModifyLavaSpeedPower extends ValueModifyingPower {
    public ModifyLavaSpeedPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }
}
