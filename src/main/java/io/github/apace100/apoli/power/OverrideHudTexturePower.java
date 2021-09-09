package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class OverrideHudTexturePower extends Power {
    private final Identifier statusBarTexture;
    public OverrideHudTexturePower(PowerType<?> type, LivingEntity entity, Identifier statusBarTexture) {
        super(type, entity);
        this.statusBarTexture = statusBarTexture;
    }

    public Identifier getStatusBarTexture() {
        return statusBarTexture;
    }
}