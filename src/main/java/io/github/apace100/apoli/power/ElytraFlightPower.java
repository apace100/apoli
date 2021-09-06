package io.github.apace100.apoli.power;

import net.adriantodt.fallflyinglib.FallFlyingLib;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class ElytraFlightPower extends PlayerAbilityPower {

    private final boolean renderElytra;
    private final Identifier textureLocation;

    public ElytraFlightPower(PowerType<?> type, LivingEntity entity, boolean renderElytra, Identifier textureLocation) {
        super(type, entity, FallFlyingLib.ABILITY);
        this.renderElytra = renderElytra;
        this.textureLocation = textureLocation;
    }

    public boolean shouldRenderElytra() {
        return renderElytra;
    }

    public Identifier getTextureLocation() {
        return textureLocation;
    }
}
