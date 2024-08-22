package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.NightVisionPowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LightmapTextureManager.class)
@Environment(EnvType.CLIENT)
public abstract class LightmapTextureManagerMixin implements AutoCloseable {

    @Shadow
    @Final
    private MinecraftClient client;

    @ModifyVariable(method = "update", at = @At("STORE"), ordinal = 6)
    private float apoli$modifyNightVisionStrength(float original) {
        return PowerHolderComponent.getPowerTypes(this.client.player, NightVisionPowerType.class)
            .stream()
            .map(NightVisionPowerType::getStrength)
            .max(Float::compareTo)
            .map(newValue -> Math.max(newValue, original))
            .orElse(original);
    }

}
