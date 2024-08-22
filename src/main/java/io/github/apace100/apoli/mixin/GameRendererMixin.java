package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
    protected abstract void loadPostProcessor(Identifier identifier);

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

        PowerHolderComponent.getPowerTypes(client.getCameraEntity(), ShaderPowerType.class)
            .stream()
            .filter(p -> resourceManager.getResource(p.getShaderLocation()).isPresent())
            .max(Comparator.comparing(ShaderPowerType::getPriority))
            .ifPresent(p -> {

                Identifier shaderLocation = p.getShaderLocation();

                loadPostProcessor(shaderLocation);
                apoli$currentlyLoadedShader = shaderLocation;

            });

    }

    @Inject(at = @At("HEAD"), method = "render")
    private void apoli$loadShaderFromPower(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {

        //  Load a shader from a shader power with a high priority
        PowerHolderComponent.getPowerTypes(client.getCameraEntity(), ShaderPowerType.class)
            .stream()
            .filter(p -> resourceManager.getResource(p.getShaderLocation()).isPresent())
            .max(Comparator.comparing(ShaderPowerType::getPriority))
            .ifPresent(p -> {
                Identifier shaderLocation = p.getShaderLocation();
                if (shaderLocation != apoli$currentlyLoadedShader) {
                    loadPostProcessor(shaderLocation);
                    apoli$currentlyLoadedShader = shaderLocation;
                }
            });

        //  Remove the currently loaded shader if the entity doesn't have any shader powers
        if (!PowerHolderComponent.hasPowerType(client.getCameraEntity(), ShaderPowerType.class) && apoli$currentlyLoadedShader != null) {

            if (postProcessor != null) {
                disablePostProcessor();
            }

            postProcessorEnabled = false;
            apoli$currentlyLoadedShader = null;

        }

    }

    @Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;hudHidden:Z"))
    private void apoli$renderOverlayPowersBelowHud(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        PowerHolderComponent.getPowerTypes(client.getCameraEntity(), OverlayPowerType.class)
            .stream()
            .filter(p -> p.shouldRender(client.options, OverlayPowerType.DrawPhase.BELOW_HUD))
            .sorted(Comparator.comparing(OverlayPowerType::getPriority))
            .forEach(OverlayPowerType::render);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", ordinal = 0))
    private void apoli$renderOverlayPowersAboveHud(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        PowerHolderComponent.getPowerTypes(client.getCameraEntity(), OverlayPowerType.class)
            .stream()
            .filter(p -> p.shouldRender(client.options, OverlayPowerType.DrawPhase.ABOVE_HUD))
            .sorted(Comparator.comparing(OverlayPowerType::getPriority))
            .forEach(OverlayPowerType::render);
    }

    @Inject(at = @At("HEAD"), method = "togglePostProcessorEnabled", cancellable = true)
    private void disableShaderToggle(CallbackInfo ci) {
        PowerHolderComponent.withPowerType(client.getCameraEntity(), ShaderPowerType.class, p -> true, shaderPower -> {
            Identifier shaderLoc = shaderPower.getShaderLocation();
            if(!shaderPower.isToggleable() && apoli$currentlyLoadedShader == shaderLoc) {
                ci.cancel();
            }
        });
    }

    // NightVisionPower
    @WrapMethod(method = "getNightVisionStrength")
    private static float apoli$modifyNightVisionStrength(LivingEntity entity, float tickDelta, Operation<Float> original) {
        return PowerHolderComponent.getPowerTypes(entity, NightVisionPowerType.class)
            .stream()
            .map(NightVisionPowerType::getStrength)
            .max(Float::compareTo)
            .orElseGet(() -> original.call(entity, tickDelta));
    }

    @ModifyExpressionValue(method = "getFov", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getSubmersionType()Lnet/minecraft/block/enums/CameraSubmersionType;"))
    private CameraSubmersionType apoli$modifySubmersionTypeFov(CameraSubmersionType original, Camera camera) {
        return PowerHolderComponent.getPowerTypes(camera.getFocusedEntity(), ModifyCameraSubmersionTypePowerType.class, true)
            .stream()
            .filter(p -> p.doesModify(original) && p.isActive())
            .findFirst()
            .map(ModifyCameraSubmersionTypePowerType::getNewType)
            .orElse(original);
    }

    @Unique
    private final HashMap<BlockPos, BlockState> savedStates = new HashMap<>();

    // PHASING: remove_blocks
    @Inject(at = @At(value = "HEAD"), method = "render")
    private void beforeRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        List<PhasingPowerType> phasings = PowerHolderComponent.getPowerTypes(camera.getFocusedEntity(), PhasingPowerType.class);
        if (phasings.stream().anyMatch(pp -> pp.getRenderType() == PhasingPowerType.RenderType.REMOVE_BLOCKS)) {
            float view = phasings.stream().filter(pp -> pp.getRenderType() == PhasingPowerType.RenderType.REMOVE_BLOCKS).map(PhasingPowerType::getViewDistance).min(Float::compareTo).get();
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
        if (PowerHolderComponent.getPowerTypes(camera.getFocusedEntity(), PhasingPowerType.class).stream().anyMatch(pp -> pp.getRenderType() == PhasingPowerType.RenderType.REMOVE_BLOCKS)) {
            camera.update(area, focusedEntity, false, false, tickDelta);
        } else {
            camera.update(area, focusedEntity, thirdPerson, inverseView, tickDelta);
        }
    }

    @Unique
    private Set<BlockPos> getEyePos(float rangeX, float rangeY, float rangeZ) {
        Vec3d pos = camera.getFocusedEntity().getPos().add(0, camera.getFocusedEntity().getEyeHeight(camera.getFocusedEntity().getPose()), 0);
        Box cameraBox = new Box(pos, pos);
        cameraBox = cameraBox.expand(rangeX, rangeY, rangeZ);
        HashSet<BlockPos> set = new HashSet<>();
        BlockPos.stream(cameraBox).forEach(p -> set.add(p.toImmutable()));
        return set;
    }

    @ModifyExpressionValue(method = "method_18144", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;canHit()Z"))
    private static boolean apoli$preventEntitySelection(boolean original, Entity target) {
        Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
        return original
            && !PowerHolderComponent.hasPowerType(cameraEntity, PreventEntitySelectionPowerType.class, p -> p.doesPrevent(target));
    }

}
