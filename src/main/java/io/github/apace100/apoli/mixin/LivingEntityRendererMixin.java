package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.InvisibilityPower;
import io.github.apace100.apoli.power.ModelColorPower;
import io.github.apace100.apoli.power.PreventFeatureRenderPower;
import io.github.apace100.apoli.power.ShakingPower;
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
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
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

    @ModifyExpressionValue(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/entity/LivingEntity;ZZZ)Lnet/minecraft/client/render/RenderLayer;"))
    private RenderLayer apoli$changeRenderLayerWhenTranslucent(@Nullable RenderLayer original, LivingEntity entity) {
        return PowerHolderComponent.hasPower(entity, ModelColorPower.class, ModelColorPower::isTranslucent)
            ? RenderLayer.getItemEntityTranslucentCull(this.getTexture(entity))
            : original;
    }

    @WrapWithCondition(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/FeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/Entity;FFFFFF)V"))
    private boolean apoli$preventFeatureRender(FeatureRenderer<?, ?> instance, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Entity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        return (!(instance instanceof ArmorFeatureRenderer<?, ?, ?>) || !PowerHolderComponent.hasPower(entity, InvisibilityPower.class, Predicate.not(InvisibilityPower::shouldRenderArmor)))
            && !PowerHolderComponent.hasPower(entity, PreventFeatureRenderPower.class, p -> p.doesApply(instance));
    }

    @WrapOperation(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void apoli$renderColorChangedModel(EntityModel<?> instance, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, Operation<Void> original, LivingEntity entity) {

        List<ModelColorPower> modelColorPowers = PowerHolderComponent.getPowers(entity, ModelColorPower.class);
        if (modelColorPowers.isEmpty()) {
            original.call(instance, matrices, vertices, light, overlay, red, green, blue, alpha);
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

        instance.render(matrices, vertices, light, overlay, newRed, newGreen, newBlue, newAlpha);

    }

}
