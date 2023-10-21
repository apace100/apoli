package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.power.ModifyEnchantmentLevelPower;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enchantment.class)
public class EnchantmentMixin {

    @ModifyExpressionValue(method = "getEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private boolean apoli$allowEmptySlotItemIfModified(boolean original, @Local ItemStack stack) {
        return original && !ModifyEnchantmentLevelPower.isInEnchantmentMap(((EntityLinkedItemStack) stack).apoli$getEntity());
    }

}
