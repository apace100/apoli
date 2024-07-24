package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import io.github.apace100.apoli.access.PseudoRenderDataHolder;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PosePower;
import io.github.apace100.apoli.power.ModelColorPower;
import io.github.apace100.apoli.util.ArmPoseReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    private PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @WrapOperation(method = "renderArm", at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", ordinal = 0), @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", ordinal = 1)})
    private void apoli$makeArmAndSleeveTransparent(ModelPart instance, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, Operation<Void> original, MatrixStack mMatrices, VertexConsumerProvider mVertexConsumers, int mLight, AbstractClientPlayerEntity mPlayer, @Local Identifier skinTextureId) {

        List<ModelColorPower> modelColorPowers = PowerHolderComponent.getPowers(mPlayer, ModelColorPower.class);
        if (modelColorPowers.isEmpty()) {
            original.call(instance, matrices, vertices, light, overlay);
            return;
        }

        float red = modelColorPowers.stream().map(ModelColorPower::getRed).reduce((a, b) -> a * b).orElse(1.0f);
        float green = modelColorPowers.stream().map(ModelColorPower::getGreen).reduce((a, b) -> a * b).orElse(1.0f);
        float blue = modelColorPowers.stream().map(ModelColorPower::getBlue).reduce((a, b) -> a * b).orElse(1.0f);
        float alpha = modelColorPowers.stream().map(ModelColorPower::getAlpha).min(Float::compare).orElse(1.0f);

        instance.render(matrices, mVertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(skinTextureId)), light, overlay, ColorHelper.Argb.fromFloats(alpha, red, green, blue));

    }

    @ModifyExpressionValue(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isFallFlying()Z"))
    private boolean apoli$forceFallFlyingPose(boolean original, AbstractClientPlayerEntity player, @Share("applyPseudoFallFlyingTicks") LocalBooleanRef applyPseudoFallFlyingTicksRef, @Share("pseudoRoll") LocalIntRef pseudoRollRef) {

        if (original || !(player instanceof PseudoRenderDataHolder renderData)) {
            return original;
        }

        int pseudoRoll = renderData.apoli$getPseudoFallFlyingTicks();
        boolean apply = pseudoRoll > 0;

        pseudoRollRef.set(pseudoRoll);
        applyPseudoFallFlyingTicksRef.set(apply);

        return apply;

    }

    @ModifyExpressionValue(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isUsingRiptide()Z"))
    private boolean apoli$accountForForcedRiptide(boolean original, AbstractClientPlayerEntity player) {
        return original || PosePower.hasEntityPose(player, EntityPose.SPIN_ATTACK);
    }

    @ModifyExpressionValue(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getFallFlyingTicks()I"))
    private int apoli$applyPseudoFallFlyingTicks(int original, AbstractClientPlayerEntity player, @Share("applyPseudoFallFlyingTicks") LocalBooleanRef applyPseudoFallFlyingTicksRef, @Share("pseudoRoll") LocalIntRef pseudoRollRef) {
        return applyPseudoFallFlyingTicksRef.get()
            ? pseudoRollRef.get()
            : original;
    }

    @ModifyReturnValue(method = "getArmPose", at = @At("RETURN"))
    private static BipedEntityModel.ArmPose apoli$overrideArmPose(BipedEntityModel.ArmPose original, AbstractClientPlayerEntity player) {
        return ArmPoseReference
            .getArmPose(player)
            .orElse(original);
    }

}
