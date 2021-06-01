package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;

public class ElytraFlightPower extends Power {

    private final boolean renderElytra;

    public ElytraFlightPower(PowerType<?> type, LivingEntity entity, boolean renderElytra) {
        super(type, entity);
        this.renderElytra = renderElytra;
    }

    public boolean shouldRenderElytra() {
        return renderElytra;
    }
}
