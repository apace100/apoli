package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.ladysnake.pal.VanillaAbilities;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class CreativeFlightPower extends Power {

    public CreativeFlightPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    @Override
    public void onAdded() {
        if(entity instanceof PlayerEntity) {
            Apoli.SCHEDULER.queue(server -> {
                Apoli.POWER_SOURCE.grantTo((PlayerEntity)entity, VanillaAbilities.ALLOW_FLYING);
                Apoli.POWER_SOURCE.grantTo((PlayerEntity)entity, VanillaAbilities.FLYING);
            }, 1);
        }
    }

    @Override
    public void onRemoved() {
        if(entity instanceof PlayerEntity) {
            Apoli.POWER_SOURCE.revokeFrom((PlayerEntity)entity, VanillaAbilities.ALLOW_FLYING);
            Apoli.POWER_SOURCE.revokeFrom((PlayerEntity)entity, VanillaAbilities.FLYING);
        }
    }
}
