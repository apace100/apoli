package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class ShaderPower extends Power {

    private final Identifier shaderLocation;

    public ShaderPower(PowerType<?> type, LivingEntity entity, Identifier shaderLocation) {
        super(type, entity);
        this.shaderLocation = shaderLocation;
    }

    public Identifier getShaderLocation() {
        return shaderLocation;
    }
}
