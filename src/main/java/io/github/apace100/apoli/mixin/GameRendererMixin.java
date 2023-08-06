package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private Camera camera;

    @Shadow
    @Final
    MinecraftClient client;

    @Shadow
    abstract void loadPostProcessor(Identifier identifier);

    @Shadow
    PostEffectProcessor postProcessor;
    @Shadow
    private boolean postProcessorEnabled;

    @Shadow
    @Final
    private ResourceManager resourceManager;

    @Shadow
    public abstract void disablePostProcessor();

    @Unique
    private Identifier apoli$currentlyLoadedShader;

    @Inject(at = @At("TAIL"), method = "onCameraEntitySet")
    private void apoli$loadShaderFromPowerOnCameraEntity(Entity entity, CallbackInfo ci) {

        PowerHolderComponent.getPowers(client.getCameraEntity(), ShaderPower.class)
            .stream()
            .filter(p -> resourceManager.getResource(p.getShaderLocation()).isPresent())
            .max(Comparator.comparing(ShaderPower::getPriority))
            .ifPresent(p -> {

                Identifier shaderLocation = p.getShaderLocation();

                loadPostProcessor(shaderLocation);
                apoli$currentlyLoadedShader = shaderLocation;

            });

    }

    @Inject(at = @At("HEAD"), method = "render")
    private void apoli$loadShaderFromPower(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {

        //  Load a shader from a shader power with a high priority
        PowerHolderComponent.getPowers(client.getCameraEntity(), ShaderPower.class)
            .stream()
            .filter(p -> resourceManager.getResource(p.getShaderLocation()).isPresent())
            .max(Comparator.comparing(ShaderPower::getPriority))
            .ifPresent(p -> {
                Identifier shaderLocation = p.getShaderLocation();
                if (shaderLocation != apoli$currentlyLoadedShader) {
                    loadPostProcessor(shaderLocation);
                    apoli$currentlyLoadedShader = shaderLocation;
                }
            });

        //  Remove the currently loaded shader if the entity doesn't have any shader powers
        if (!PowerHolderComponent.hasPower(client.getCameraEntity(), ShaderPower.class) && apoli$currentlyLoadedShader != null) {

            if (postProcessor != null) {
                disablePostProcessor();
            }

            postProcessorEnabled = false;
            apoli$currentlyLoadedShader = null;

        }

    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", ordinal = 0))
    private void apoli$renderOverlayPowersBelowHud(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {

        //  Skip this method if the HUD is not hidden or if the current screen is not null
        //  (to make sure the overlay is not rendered twice)
        if (!client.options.hudHidden || client.currentScreen != null) {
            return;
        }

        //  Otherwise, render overlay powers specified to render below the HUD
        PowerHolderComponent.getPowers(client.getCameraEntity(), OverlayPower.class)
            .stream()
            .filter(p -> p.shouldRender(client.options, OverlayPower.DrawPhase.BELOW_HUD))
            .sorted(Comparator.comparing(OverlayPower::getPriority))
            .forEach(OverlayPower::render);

    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V"))
    private void apoli$renderOverlayPowersAboveHud(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        PowerHolderComponent.getPowers(client.getCameraEntity(), OverlayPower.class)
            .stream()
            .filter(p -> p.shouldRender(client.options, OverlayPower.DrawPhase.ABOVE_HUD))
            .sorted(Comparator.comparing(OverlayPower::getPriority))
            .forEach(OverlayPower::render);
    }

    @Inject(at = @At("HEAD"), method = "togglePostProcessorEnabled", cancellable = true)
    private void disableShaderToggle(CallbackInfo ci) {
        PowerHolderComponent.withPower(client.getCameraEntity(), ShaderPower.class, null, shaderPower -> {
            Identifier shaderLoc = shaderPower.getShaderLocation();
            if(!shaderPower.isToggleable() && apoli$currentlyLoadedShader == shaderLoc) {
                ci.cancel();
            }
        });
    }

    // NightVisionPower
    @Inject(at = @At("HEAD"), method = "getNightVisionStrength", cancellable = true)
    private static void getNightVisionStrength(LivingEntity livingEntity, float f, CallbackInfoReturnable<Float> info) {
        if (livingEntity instanceof PlayerEntity && !livingEntity.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            List<NightVisionPower> nvs = PowerHolderComponent.KEY.get(livingEntity).getPowers(NightVisionPower.class);
            Optional<Float> strength = nvs.stream().filter(NightVisionPower::isActive).map(NightVisionPower::getStrength).max(Float::compareTo);
            strength.ifPresent(info::setReturnValue);
        }
    }

    @Redirect(method = "getFov", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getSubmersionType()Lnet/minecraft/client/render/CameraSubmersionType;"))
    private CameraSubmersionType modifySubmersionType(Camera camera) {
        CameraSubmersionType original = camera.getSubmersionType();
        if(camera.getFocusedEntity() instanceof LivingEntity) {
            for(ModifyCameraSubmersionTypePower p : PowerHolderComponent.getPowers(camera.getFocusedEntity(), ModifyCameraSubmersionTypePower.class)) {
                if(p.doesModify(original)) {
                    return p.getNewType();
                }
            }
        }
        return original;
    }

    private HashMap<BlockPos, BlockState> savedStates = new HashMap<>();

    // PHASING: remove_blocks
    @Inject(at = @At(value = "HEAD"), method = "render")
    private void beforeRender(float tickDelta, long startTime, boolean tick, CallbackInfo info) {
        List<PhasingPower> phasings = PowerHolderComponent.getPowers(camera.getFocusedEntity(), PhasingPower.class);
        if (phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.REMOVE_BLOCKS)) {
            float view = phasings.stream().filter(pp -> pp.getRenderType() == PhasingPower.RenderType.REMOVE_BLOCKS).map(PhasingPower::getViewDistance).min(Float::compareTo).get();
            Set<BlockPos> eyePositions = getEyePos(0.25F, 0.05F, 0.25F);
            Set<BlockPos> noLongerEyePositions = new HashSet<>();
            for (BlockPos p : savedStates.keySet()) {
                if (!eyePositions.contains(p)) {
                    noLongerEyePositions.add(p);
                }
            }
            for (BlockPos eyePosition : noLongerEyePositions) {
                BlockState state = savedStates.get(eyePosition);
                client.world.setBlockState(eyePosition, state);
                savedStates.remove(eyePosition);
            }
            for (BlockPos p : eyePositions) {
                BlockState stateAtP = client.world.getBlockState(p);
                if (!savedStates.containsKey(p) && !client.world.isAir(p) && !(stateAtP.getBlock() instanceof FluidBlock)) {
                    savedStates.put(p, stateAtP);
                    client.world.setBlockState(p, Blocks.AIR.getDefaultState());
                }
            }
        } else if (savedStates.size() > 0) {
            Set<BlockPos> noLongerEyePositions = new HashSet<>(savedStates.keySet());
            for (BlockPos eyePosition : noLongerEyePositions) {
                BlockState state = savedStates.get(eyePosition);
                client.world.setBlockState(eyePosition, state);
                savedStates.remove(eyePosition);
            }
        }
    }

    // PHASING
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V"), method = "renderWorld")
    private void preventThirdPerson(Camera camera, BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta) {
        if (PowerHolderComponent.getPowers(camera.getFocusedEntity(), PhasingPower.class).stream().anyMatch(pp -> pp.getRenderType() == PhasingPower.RenderType.REMOVE_BLOCKS)) {
            camera.update(area, focusedEntity, false, false, tickDelta);
        } else {
            camera.update(area, focusedEntity, thirdPerson, inverseView, tickDelta);
        }
    }

    private Set<BlockPos> getEyePos(float rangeX, float rangeY, float rangeZ) {
        Vec3d pos = camera.getFocusedEntity().getPos().add(0, camera.getFocusedEntity().getEyeHeight(camera.getFocusedEntity().getPose()), 0);
        Box cameraBox = new Box(pos, pos);
        cameraBox = cameraBox.expand(rangeX, rangeY, rangeZ);
        HashSet<BlockPos> set = new HashSet<>();
        BlockPos.stream(cameraBox).forEach(p -> set.add(p.toImmutable()));
        return set;
    }
}
