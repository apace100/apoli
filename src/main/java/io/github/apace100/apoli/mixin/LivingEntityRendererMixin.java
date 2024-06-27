package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import io.github.apace100.apoli.access.PseudoRenderDataHolder;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Predicate;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin extends EntityRenderer<LivingEntity> {

    protected LivingEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @ModifyReturnValue(method = "isShaking", at = @At("RETURN"))
    private boolean apoli$letEntitiesShakeTheirBodies(boolean original, LivingEntity entity) {
        return original || PowerHolderComponent.hasPower(entity, ShakingPower.class);
    }

    @ModifyExpressionValue(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;hasOutline(Lnet/minecraft/entity/Entity;)Z"))
    private boolean apoli$preventOutlineWhenInvisible(boolean original, LivingEntity entity) {
        return !PowerHolderComponent.hasPower(entity, InvisibilityPower.class, Predicate.not(InvisibilityPower::shouldRenderOutline)) && original;
    }

    @WrapOperation(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/entity/LivingEntity;ZZZ)Lnet/minecraft/client/render/RenderLayer;"))
    private RenderLayer apoli$useTranslucentRenderLayerWhenVisible(LivingEntityRenderer<?, ?> renderer, LivingEntity entity, boolean showBody, boolean translucent, boolean showOutline, Operation<RenderLayer> original) {
        return original.call(renderer, entity, showBody, translucent || showBody && PowerHolderComponent.hasPower(entity, ModelColorPower.class, ModelColorPower::isTranslucent), showOutline);
    }

    @WrapWithCondition(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/FeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/Entity;FFFFFF)V"))
    private boolean apoli$preventFeatureRender(FeatureRenderer<?, ?> instance, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Entity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        return (!(instance instanceof ArmorFeatureRenderer<?, ?, ?>) || !PowerHolderComponent.hasPower(entity, InvisibilityPower.class, Predicate.not(InvisibilityPower::shouldRenderArmor)))
            && !PowerHolderComponent.hasPower(entity, PreventFeatureRenderPower.class, p -> p.doesApply(instance));
    }

    @WrapOperation(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void apoli$renderColorChangedModel(EntityModel<?> model, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, Operation<Void> original, LivingEntity entity) {

        List<ModelColorPower> modelColorPowers = PowerHolderComponent.getPowers(entity, ModelColorPower.class);
        if (modelColorPowers.isEmpty()) {
            original.call(model, matrices, vertices, light, overlay, red, green, blue, alpha);
            return;
        }

        //  TODO: Implement custom blending modes for blending colors
        float newRed = modelColorPowers
            .stream()
            .map(ModelColorPower::getRed)
            .reduce(red, (a, b) -> a * b);
        float newGreen = modelColorPowers
            .stream()
            .map(ModelColorPower::getGreen)
            .reduce(green, (a, b) -> a * b);
        float newBlue = modelColorPowers
            .stream()
            .map(ModelColorPower::getBlue)
            .reduce(blue, (a, b) -> a * b);
        float newAlpha = modelColorPowers
            .stream()
            .map(ModelColorPower::getAlpha)
            .min(Float::compareTo)
            .map(alphaFactor -> alpha * alphaFactor)
            .orElse(alpha);

        original.call(model, matrices, vertices, light, overlay, newRed, newGreen, newBlue, newAlpha);

    }

    @ModifyExpressionValue(method = "setupTransforms", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isUsingRiptide()Z"))
    private boolean apoli$forceRiptidePose(boolean original, LivingEntity entity) {
        return original || PosePower.hasEntityPose(entity, EntityPose.SPIN_ATTACK);
    }

    @ModifyExpressionValue(method = "setupTransforms", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;deathTime:I", ordinal = 0))
    private int apoli$forceDyingPose(int original, LivingEntity entity, @Share("applyPseudoDeathTicks") LocalBooleanRef applyPseudoDeathTicksRef, @Share("pseudoDeathTicks") LocalIntRef pseudoDeathTicksRef) {

        if (original > 0 || !(entity instanceof PseudoRenderDataHolder renderData)) {
            return original;
        }

        int pseudoDeathTicks = renderData.apoli$getPseudoDeathTicks();

        pseudoDeathTicksRef.set(pseudoDeathTicks);
        applyPseudoDeathTicksRef.set(pseudoDeathTicks > 0);

        return pseudoDeathTicks;

    }

    @ModifyExpressionValue(method = "setupTransforms", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;deathTime:I", ordinal = 1))
    private int apoli$applyPseudoDeathTicks(int original, LivingEntity entity, @Share("applyPseudoDeathTicks") LocalBooleanRef applyPseudoDeathTicksRef, @Share("pseudoDeathTicks") LocalIntRef pseudoDeathTicksRef) {
        return applyPseudoDeathTicksRef.get()
            ? pseudoDeathTicksRef.get()
            : original;
    }

}
