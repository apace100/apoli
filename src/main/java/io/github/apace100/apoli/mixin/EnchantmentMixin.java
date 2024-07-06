package io.github.apace100.apoli.mixin;

import net.minecraft.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Enchantment.class)
public class EnchantmentMixin {

    //  TODO: Fix this alongside the `modify_enchantment_level` power type -eggohito
//    @ModifyExpressionValue(method = "getEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
//    private boolean apoli$allowEmptySlotItemIfModified(boolean original, @Local ItemStack stack) {
//        return original && !ModifyEnchantmentLevelPower.isInEnchantmentMap(((EntityLinkedItemStack) stack).apoli$getEntity());
//    }

}
