package io.github.apace100.apoli.power;

import net.adriantodt.fallflyinglib.FallFlyingLib;
import net.minecraft.entity.LivingEntity;

public class ElytraFlightPower extends PlayerAbilityPower {

    private final boolean renderElytra;

    public ElytraFlightPower(PowerType<?> type, LivingEntity entity, boolean renderElytra) {
        super(type, entity, FallFlyingLib.ABILITY);
        this.renderElytra = renderElytra;
    }

    public boolean shouldRenderElytra() {
        return renderElytra;
    }
}
