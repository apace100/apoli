package io.github.apace100.apoli.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void renderOnHud(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        // TODO: Make PowerHudRenderers draw here :)
        // TODO: use this place to add an "OverlayPower" thingy
    }
}
