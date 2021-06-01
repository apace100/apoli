package io.github.apace100.apoli.power;

import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class SetEntityGroupPower extends Power {

    public final EntityGroup group;

    public SetEntityGroupPower(PowerType<?> type, LivingEntity entity, EntityGroup group) {
        super(type, entity);
        this.group = group;
    }
}
