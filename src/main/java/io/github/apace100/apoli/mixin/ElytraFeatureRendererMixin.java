package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ElytraFlightPowerType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ElytraFeatureRenderer.class)
public class ElytraFeatureRendererMixin {

    @ModifyExpressionValue(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean apoli$wearingElytraProxy(boolean original, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int i, LivingEntity entity) {
        return original
            || PowerHolderComponent.hasPowerType(entity, ElytraFlightPowerType.class, ElytraFlightPowerType::shouldRenderElytra);
    }

    @WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getArmorCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"))
    private RenderLayer apoli$overrideElytraTexture(Identifier texture, Operation<RenderLayer> original, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, LivingEntity entity) {
        return original.call(PowerHolderComponent.getPowerTypes(entity, ElytraFlightPowerType.class, true)
            .stream()
            .filter(p -> p.getTextureLocation() != null && p.isActive())
            .findFirst()
            .map(ElytraFlightPowerType::getTextureLocation)
            .orElse(texture));
    }

}
