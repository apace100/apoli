package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;

public class InvisibilityPower extends Power {

    private final boolean renderArmor;

    public InvisibilityPower(PowerType<?> type, LivingEntity entity, boolean renderArmor) {
        super(type, entity);
        this.renderArmor = renderArmor;
    }

    public boolean shouldRenderArmor() {
        return renderArmor;
    }
}
