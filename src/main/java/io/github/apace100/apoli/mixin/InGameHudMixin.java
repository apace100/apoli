package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.OverlayPower;
import io.github.apace100.apoli.power.OverrideHudTexturePower;
import io.github.apace100.apoli.screen.GameHudRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalInt;

@Mixin(InGameHud.class)
@Environment(EnvType.CLIENT)
public abstract class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract PlayerEntity getCameraPlayer();

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

    @Unique
    private Optional<OverrideHudTexturePower> apoli$getOverrideHudTexturePower() {
        return PowerHolderComponent.getPowers(this.client.player, OverrideHudTexturePower.class)
            .stream()
            .max(Comparator.comparing(OverrideHudTexturePower::getPriority));
    }
    
    @WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getArmor()I", shift = At.Shift.AFTER)))
    private void apoli$overrideFullArmorSprite(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 34, 9, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 1), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getArmor()I", shift = At.Shift.AFTER)))
    private void apoli$overrideHalfArmorSprite(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 25, 9, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 2), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getArmor()I", shift = At.Shift.AFTER)))
    private void apoli$overrideEmptyArmorSprite(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 16, 9, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getHungerManager()Lnet/minecraft/entity/player/HungerManager;", ordinal = 1, shift = At.Shift.AFTER)))
    private void apoli$overrideEmptyFoodSprite(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, this.getCameraPlayer().hasStatusEffect(StatusEffects.HUNGER) ? 133 : 16, 27, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 1), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getHungerManager()Lnet/minecraft/entity/player/HungerManager;", ordinal = 1, shift = At.Shift.AFTER)))
    private void apoli$overrideFullFoodSprite(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, this.getCameraPlayer().hasStatusEffect(StatusEffects.HUNGER) ? 88 : 52, 27, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 2), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getHungerManager()Lnet/minecraft/entity/player/HungerManager;", ordinal = 1, shift = At.Shift.AFTER)))
    private void apoli$overrideHalfFoodSprite(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, this.getCameraPlayer().hasStatusEffect(StatusEffects.HUNGER) ? 97 : 61, 27, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z")))
    private void apoli$overrideBubbleSprite(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 16, 18, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 1), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z")))
    private void apoli$overrideBurstingBubbleSprite(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 25, 18, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "drawHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private void apoli$overrideHeartSprite(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original, DrawContext context, InGameHud.HeartType type, int mX, int mY, boolean hardcore, boolean blinking, boolean half) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawHeartTexture(instance, type, x, y, width, height, hardcore, blinking, half),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private void apoli$overrideBaseExperienceBarSprite(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawBarTexture(instance, texture, x, y, 0, 64, width, height, false, OptionalInt.empty(), OptionalInt.of(5)),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIIIIIII)V"))
    private void apoli$overrideProgressExperienceBarSprite(DrawContext instance, Identifier texture, int i, int j, int k, int l, int x, int y, int width, int height, Operation<Void> original, @Local(ordinal = 1) int experienceProgress) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawBarTexture(instance, texture, x, y, 0, 64, width, height, experienceProgress > 0, OptionalInt.empty(), OptionalInt.of(5)),
            () -> original.call(instance, texture, i, j, k, l, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0))
    private void apoli$overrideCrosshairSprite(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 0, 0, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getAttackIndicator()Lnet/minecraft/client/option/SimpleOption;"), to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIIIIIII)V")))
    private void apoli$overrideFullCrosshairAttackIndicatorSprite(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 68, 94, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 1), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getAttackIndicator()Lnet/minecraft/client/option/SimpleOption;"), to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIIIIIII)V")))
    private void apoli$overrideBaseCrosshairAttackIndicatorSprite(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 36, 94, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIIIIIII)V"))
    private void apoli$overrideCrosshairAttackIndicatorProgressSprite(DrawContext instance, Identifier texture, int i, int j, int k, int l, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawBarTexture(instance, texture, x, y, 36, 94, width, height, true, OptionalInt.of(16), OptionalInt.empty()),
            () -> original.call(instance, texture, i, j, k, l, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderMountJumpBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private void apoli$overrideBaseMountJumpBarSprite(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawBarTexture(instance, texture, x, y, 0, 84, width, height, false, OptionalInt.empty(), OptionalInt.empty()),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

   @WrapOperation(method = "renderMountJumpBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIIIIIII)V"))
    private void apoli$overrideMountJumpBarProgressSprite(DrawContext instance, Identifier texture, int i, int j, int k, int l, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawBarTexture(instance, texture, x, y, 0, 84, width, height, true, OptionalInt.empty(), OptionalInt.of(5)),
            () -> original.call(instance, texture, i, j, k, l, x, y, width, height)
        );
   }

   @WrapOperation(method = "renderMountHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0))
    private void apoli$overrideEmptyMountHeartSprite(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 52, 9, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
   }

    @WrapOperation(method = "renderMountHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 1))
    private void apoli$overrideFullMountHeartSprite(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 88, 9, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

    @WrapOperation(method = "renderMountHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 2))
    private void apoli$overrideHalfMountHeartSprite(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        apoli$getOverrideHudTexturePower().ifPresentOrElse(
            p -> p.drawTexture(instance, texture, x, y, 97, 9, width, height),
            () -> original.call(instance, texture, x, y, width, height)
        );
    }

}
