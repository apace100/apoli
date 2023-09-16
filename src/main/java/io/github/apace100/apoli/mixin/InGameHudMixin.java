package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.OverlayPower;
import io.github.apace100.apoli.power.OverrideHudTexturePower;
import io.github.apace100.apoli.screen.GameHudRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.Optional;

@Mixin(InGameHud.class)
@Environment(EnvType.CLIENT)
public class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;getCurrentGameMode()Lnet/minecraft/world/GameMode;", ordinal = 0))
    private void apoli$renderOnHud(DrawContext context, float tickDelta, CallbackInfo ci) {

        //  Render overlay powers over the vanilla overlays and below the vanilla status bars
        PowerHolderComponent.getPowers(client.getCameraEntity(), OverlayPower.class)
            .stream()
            .filter(p -> p.shouldRender(client.options, OverlayPower.DrawPhase.BELOW_HUD))
            .sorted(Comparator.comparing(OverlayPower::getPriority))
            .forEach(OverlayPower::render);

        //  Render resource bars
        for(GameHudRender hudRender : GameHudRender.HUD_RENDERS) {
            hudRender.render(context, tickDelta);
        }

    }

    @ModifyArg(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"), index = 0)
    public Identifier changeStatusBarTextures(Identifier original) {
        Optional<OverrideHudTexturePower> power = PowerHolderComponent.getPowers(this.client.player, OverrideHudTexturePower.class).stream().findFirst();
        if (power.isPresent()) {
            return power.get().getStatusBarTexture();
        }
        return original;
    }

    @ModifyArg(method = "drawHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"), index = 0)
    public Identifier changeHearts(Identifier original)
    {
        Optional<OverrideHudTexturePower> power = PowerHolderComponent.getPowers(this.client.player, OverrideHudTexturePower.class).stream().findFirst();
        if (power.isPresent()) {
            return power.get().getStatusBarTexture();
        }
        return original;
    }

    @ModifyArg(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"), index = 0)
    public Identifier changeXpBarTextures(Identifier original) {
        Optional<OverrideHudTexturePower> power = PowerHolderComponent.getPowers(this.client.player, OverrideHudTexturePower.class).stream().findFirst();
        if (power.isPresent()) {
            return power.get().getStatusBarTexture();
        }
        return original;
    }

    @ModifyArg(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"), index = 0)
    public Identifier changeCrosshair(Identifier original) {
        Optional<OverrideHudTexturePower> power = PowerHolderComponent.getPowers(this.client.player, OverrideHudTexturePower.class).stream().findFirst();
        if (power.isPresent()) {
            return power.get().getStatusBarTexture();
        }
        return original;
    }

    @ModifyArg(method = "renderMountJumpBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"), index = 0)
    public Identifier changeMountJumpBar(Identifier original) {
        Optional<OverrideHudTexturePower> power = PowerHolderComponent.getPowers(this.client.player, OverrideHudTexturePower.class).stream().findFirst();
        if (power.isPresent()) {
            return power.get().getStatusBarTexture();
        }
        return original;
    }

    @ModifyArg(method = "renderMountHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"), index = 0)
    public Identifier changeMountHealth(Identifier original) {
        Optional<OverrideHudTexturePower> power = PowerHolderComponent.getPowers(this.client.player, OverrideHudTexturePower.class).stream().findFirst();
        if (power.isPresent()) {
            return power.get().getStatusBarTexture();
        }
        return original;
    }
}
