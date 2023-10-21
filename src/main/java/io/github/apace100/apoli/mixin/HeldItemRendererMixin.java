package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.access.PotentiallyEdibleItemStack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @ModifyExpressionValue(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z", ordinal = 1))
    private boolean apoli$overrideCrossbowPullbackAnimation(boolean original, AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item) {
        return original && ((PotentiallyEdibleItemStack) item).apoli$getFoodComponent().isEmpty();
    }

}
