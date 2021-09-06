package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ElytraFlightPower;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ElytraFeatureRenderer.class)
public class ElytraFeatureRendererMixin {
    @Unique
    private LivingEntity livingEntity;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean modifyEquippedStackToElytra(ItemStack itemStack, Item item, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l) {
        this.livingEntity = livingEntity;
        if(PowerHolderComponent.getPowers(livingEntity, ElytraFlightPower.class).stream().anyMatch(ElytraFlightPower::shouldRenderElytra) && !livingEntity.isInvisible()) {
            return true;
        }
        return itemStack.isOf(item);
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getArmorCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"))
    private Identifier setTexture(Identifier identifier) {
        for (ElytraFlightPower power : PowerHolderComponent.getPowers(this.livingEntity, ElytraFlightPower.class)) {
            if (power.getTextureLocation() != null) {
                return power.getTextureLocation();
            }
        }
        return identifier;
    }
}
