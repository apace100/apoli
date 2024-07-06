package io.github.apace100.apoli.mixin;

import net.minecraft.predicate.item.ItemPredicate;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemPredicate.class)
public class ItemPredicateMixin {

    //  TODO: Fix this alongside the `modify_enchantment_level` power type -eggohito
//    @ModifyExpressionValue(method = "test", at = {@At(value = "INVOKE", target = "Lnet/minecraft/item/EnchantedBookItem;getEnchantmentNbt(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/nbt/NbtList;"), @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getEnchantments()Lnet/minecraft/nbt/NbtList;")})
//    private NbtList apoli$getEnchantments(NbtList original, ItemStack stack) {
//        return ModifyEnchantmentLevelPower.getEnchantments(stack, original);
//    }

}
