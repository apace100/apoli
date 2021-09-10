package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.OverrideHudTexturePower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.screen.GameHudRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(InGameHud.class)
@Environment(EnvType.CLIENT)
public class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;getCurrentGameMode()Lnet/minecraft/world/GameMode;"))
    private void renderOnHud(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        for(GameHudRender hudRender : GameHudRender.HUD_RENDERS) {
            hudRender.render(matrices, tickDelta);
        }
    }

    @ModifyArg(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V", ordinal = 1),
            index = 1
    )
    public Identifier changeStatusBarTextures(Identifier original) {
        Optional<OverrideHudTexturePower> power = PowerHolderComponent.getPowers(this.client.player, OverrideHudTexturePower.class).stream().filter(Power::isActive).findFirst();
        if (power.isPresent()) {
            return power.get().getStatusBarTexture();
        }
        return original;
    }
}
