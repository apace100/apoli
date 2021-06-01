package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ModifyExhaustionPower extends ValueModifyingPower {

    public ModifyExhaustionPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }
}
