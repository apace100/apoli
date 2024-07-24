package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyCameraSubmersionTypePower;
import io.github.apace100.apoli.power.NightVisionPower;
import io.github.apace100.apoli.power.PhasingPower;
import io.github.apace100.apoli.util.MiscUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(BackgroundRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class BackgroundRendererMixin {

    @Shadow private static float red;

    @Shadow private static float green;

    @Shadow private static float blue;

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal = 0))
    private static boolean apoli$nightVisionProxy(boolean original, @Local Entity cameraFocusedEntity) {
        return original
            || PowerHolderComponent.hasPower(cameraFocusedEntity, NightVisionPower.class);
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getSubmersionType()Lnet/minecraft/block/enums/CameraSubmersionType;"))
    private static CameraSubmersionType apoli$modifyCameraSubmersionType(CameraSubmersionType original, Camera camera) {
        return PowerHolderComponent.getPowers(camera.getFocusedEntity(), ModifyCameraSubmersionTypePower.class, true)
            .stream()
            .filter(p -> p.doesModify(original) && p.isActive())
            .findFirst()
            .map(ModifyCameraSubmersionTypePower::getNewType)
            .orElse(original);
    }

    @ModifyExpressionValue(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getSubmersionType()Lnet/minecraft/block/enums/CameraSubmersionType;"))
    private static CameraSubmersionType apoli$modifyCameraSubmersionTypeFog(CameraSubmersionType original, Camera camera) {
        return PowerHolderComponent.getPowers(camera.getFocusedEntity(), ModifyCameraSubmersionTypePower.class, true)
            .stream()
            .filter(p -> p.doesModify(original) && p.isActive())
            .findFirst()
            .map(ModifyCameraSubmersionTypePower::getNewType)
            .orElse(original);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;"))
    private static void modifyFogColor(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfo ci) {
        if(camera.getFocusedEntity() instanceof LivingEntity) {
            if(PowerHolderComponent.getPowers(camera.getFocusedEntity(), PhasingPower.class).stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
                if(MiscUtil.getInWallBlockState((PlayerEntity)camera.getFocusedEntity()) != null) {
                    red = 0f;
                    green = 0f;
                    blue = 0f;
                }
            }
        }
    }

    @Inject(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void modifyFogData(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci, CameraSubmersionType cameraSubmersionType, Entity entity, BackgroundRenderer.FogData fogData) {
        if(camera.getFocusedEntity() instanceof LivingEntity) {
            List<PhasingPower> phasings = PowerHolderComponent.getPowers(camera.getFocusedEntity(), PhasingPower.class);
            if(phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
                if(MiscUtil.getInWallBlockState((LivingEntity)camera.getFocusedEntity()) != null) {
                    float view = phasings.stream().filter(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS).map(PhasingPower::getViewDistance).min(Float::compareTo).get();
                    if (fogData.fogType == BackgroundRenderer.FogType.FOG_SKY) {
                        fogData.fogStart = 0.0f;
                        fogData.fogEnd = view * 0.8f;
                    } else {
                        fogData.fogStart = view * 0.25f;
                        fogData.fogEnd = view;
                    }
                }
            }
        }
    }
}
