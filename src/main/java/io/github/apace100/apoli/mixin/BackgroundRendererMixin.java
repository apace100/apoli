package io.github.apace100.apoli.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyCameraSubmersionTypePower;
import io.github.apace100.apoli.power.NightVisionPower;
import io.github.apace100.apoli.power.PhasingPower;
import io.github.apace100.apoli.util.MiscUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(BackgroundRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class BackgroundRendererMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z", ordinal = 0), method = "render")
    private static boolean hasStatusEffectProxy(LivingEntity player, StatusEffect effect) {
        if(player instanceof PlayerEntity && effect == StatusEffects.NIGHT_VISION && !player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            return PowerHolderComponent.KEY.get(player).getPowers(NightVisionPower.class).stream().anyMatch(NightVisionPower::isActive);
        }
        return player.hasStatusEffect(effect);
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getFocusedEntity()Lnet/minecraft/entity/Entity;", ordinal = 0), ordinal = 0)
    private static CameraSubmersionType modifyCameraSubmersionTypeRender(CameraSubmersionType original, Camera camera) {
        if(camera.getFocusedEntity() instanceof LivingEntity) {
            for(ModifyCameraSubmersionTypePower p : PowerHolderComponent.getPowers(camera.getFocusedEntity(), ModifyCameraSubmersionTypePower.class)) {
                if(p.doesModify(original)) {
                    return p.getNewType();
                }
            }
        }
        return original;
    }

    @ModifyVariable(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getFocusedEntity()Lnet/minecraft/entity/Entity;", ordinal = 0), ordinal = 0)
    private static CameraSubmersionType modifyCameraSubmersionTypeFog(CameraSubmersionType original, Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog) {
        if(camera.getFocusedEntity() instanceof LivingEntity) {
            for(ModifyCameraSubmersionTypePower p : PowerHolderComponent.getPowers(camera.getFocusedEntity(), ModifyCameraSubmersionTypePower.class)) {
                if(p.doesModify(original)) {
                    return p.getNewType();
                }
            }
        }
        return original;
    }

    // TODO
    /*
    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z", ordinal = 0, shift = At.Shift.AFTER), ordinal = 2)
    private static float modifyFogDensityForPhasingBlindness(float original, Camera camera) {
        if(camera.getFocusedEntity() instanceof LivingEntity) {
            if(PowerHolderComponent.getPowers(camera.getFocusedEntity(), PhasingPower.class).stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
                if(MiscUtil.getInWallBlockState((PlayerEntity)camera.getFocusedEntity()) != null) {
                    return 0;
                }
            }
        }
        return original;
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getFocusedEntity()Lnet/minecraft/entity/Entity;", ordinal = 1), ordinal = 0)
    private static float modifyD(float original, Camera camera) {
        if(camera.getFocusedEntity() instanceof LivingEntity) {
            if(PowerHolderComponent.getPowers(camera.getFocusedEntity(), PhasingPower.class).stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
                if(MiscUtil.getInWallBlockState((PlayerEntity)camera.getFocusedEntity()) != null) {
                    RenderSystem.setShaderFogColor(0f, 0f, 0f);
                    return 0;
                }
            }
        }
        return original;
    }

    @ModifyVariable(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V"), ordinal = 2)
    private static float modifyFogEndForPhasingBlindness(float original, Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog) {
        if(camera.getFocusedEntity() instanceof LivingEntity) {
            List<PhasingPower> phasings = PowerHolderComponent.getPowers(camera.getFocusedEntity(), PhasingPower.class);
            if(phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
                if(MiscUtil.getInWallBlockState((LivingEntity)camera.getFocusedEntity()) != null) {
                    float view = phasings.stream().filter(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS).map(PhasingPower::getViewDistance).min(Float::compareTo).get();
                    float s;
                    if (fogType == BackgroundRenderer.FogType.FOG_SKY) {
                        s = Math.min(view * 0.8f, original);
                    } else {
                        s = Math.min(view, original);
                    }
                    return s;
                }
            }
        }
        return original;
    }*/

    @Redirect(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V"))
    private static void redirectFogStart(float start, Camera camera, BackgroundRenderer.FogType fogType) {
        if(camera.getFocusedEntity() instanceof LivingEntity) {
            List<PhasingPower> phasings = PowerHolderComponent.getPowers(camera.getFocusedEntity(), PhasingPower.class);
            if(phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS)) {
                if(MiscUtil.getInWallBlockState((LivingEntity)camera.getFocusedEntity()) != null) {
                    float view = phasings.stream().filter(pp -> pp.getRenderType() == PhasingPower.RenderType.BLINDNESS).map(PhasingPower::getViewDistance).min(Float::compareTo).get();
                    float s;
                    if (fogType == BackgroundRenderer.FogType.FOG_SKY) {
                        s = Math.min(0F, start);
                    } else {
                        if(camera.getSubmersionType() == CameraSubmersionType.WATER) {
                            s = Math.min(-4.0f, start);
                        } else {
                            s = Math.min(view * 0.25F, start);
                        }
                    }
                    RenderSystem.setShaderFogStart(s);
                    return;
                }
            }
        }
        RenderSystem.setShaderFogStart(start);
    }
}
