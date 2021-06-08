package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import net.adriantodt.fallflyinglib.FallFlyingLib;
import net.adriantodt.fallflyinglib.impl.FallFlyingAbilityTracker;
import net.adriantodt.fallflyinglib.impl.FallFlyingPipeline;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ElytraFlightPower extends Power {

    private final boolean renderElytra;

    public ElytraFlightPower(PowerType<?> type, LivingEntity entity, boolean renderElytra) {
        super(type, entity);
        this.renderElytra = renderElytra;
    }

    public boolean shouldRenderElytra() {
        return renderElytra;
    }

    @Override
    public void onAdded() {
        if(entity instanceof PlayerEntity) {
            Apoli.ELYTRA_FLIGHT_SOURCE.grantTo((PlayerEntity)entity, FallFlyingLib.ABILITY);
        }
    }

    @Override
    public void onRemoved() {
        if(entity instanceof PlayerEntity) {
            Apoli.ELYTRA_FLIGHT_SOURCE.revokeFrom((PlayerEntity)entity, FallFlyingLib.ABILITY);
        }
    }
}
