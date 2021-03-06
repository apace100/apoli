package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PhasingPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {

    @Inject(method = "renderInWallOverlay", at = @At("HEAD"), cancellable = true)
    private static void preventInWallOverlayRendering(Sprite sprite, MatrixStack matrixStack, CallbackInfo ci) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if(minecraftClient.cameraEntity != null) {
            if(PowerHolderComponent.getPowers(minecraftClient.cameraEntity, PhasingPower.class).size() > 0) {
                ci.cancel();
            }
        }
    }
}
