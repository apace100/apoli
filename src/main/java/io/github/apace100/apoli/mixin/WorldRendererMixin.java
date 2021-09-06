package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.EntityGlowPower;
import io.github.apace100.apoli.power.SelfGlowPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Iterator;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Final
    @Shadow
    private MinecraftClient client;

    @Unique
    private Entity renderEntity;

    @Shadow public abstract void reload();

    @Shadow protected abstract void updateChunks(long limitTime);

    @Shadow private boolean needsTerrainUpdate;

    @Shadow public abstract void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f);

    @Inject(method = "render", at = @At("HEAD"))
    private void updateChunksIfRenderChanged(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        if(ApoliClient.shouldReloadWorldRenderer) {
            reload();
            ApoliClient.shouldReloadWorldRenderer = false;
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/Entity;getTeamColorValue()I"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void getEntity(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci, Profiler profiler, Vec3d vec3d, double d, double e, double f, Matrix4f matrix4f2, boolean bl, Frustum frustum2, boolean bl3, VertexConsumerProvider.Immediate immediate, Iterator var39, Entity entity) {
        this.renderEntity = entity;
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;setColor(IIII)V"))
    private void setColors(Args args) {
        for (EntityGlowPower power : PowerHolderComponent.getPowers(client.getCameraEntity(), EntityGlowPower.class)) {
            if (power.doesApply(renderEntity)) {
                if (!power.usesTeams()) {
                    args.set(0, (int)(power.getRed() * 255.0F));
                    args.set(1, (int)(power.getGreen() * 255.0F));
                    args.set(2, (int)(power.getBlue() * 255.0F));
                }
            }
        }
        for (SelfGlowPower power : PowerHolderComponent.getPowers(renderEntity, SelfGlowPower.class)) {
            if (!power.usesTeams()) {
                args.set(0, (int)(power.getRed() * 255.0F));
                args.set(1, (int)(power.getGreen() * 255.0F));
                args.set(2, (int)(power.getBlue() * 255.0F));
            }
        }
    }
}
