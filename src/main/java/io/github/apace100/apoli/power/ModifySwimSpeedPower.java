package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ModifySwimSpeedPower extends ValueModifyingPower {
    public ModifySwimSpeedPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }
}
