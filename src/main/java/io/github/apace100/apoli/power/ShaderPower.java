package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class ShaderPower extends Power {

    private final Identifier shaderLocation;
    private final boolean toggleable;

    public ShaderPower(PowerType<?> type, LivingEntity entity, Identifier shaderLocation, boolean toggleable) {
        super(type, entity);
        this.shaderLocation = shaderLocation;
        this.toggleable = toggleable;
    }

    public Identifier getShaderLocation() {
        return shaderLocation;
    }

    public boolean isToggleable() {
        return toggleable;
    }
}
