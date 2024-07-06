package io.github.apace100.apoli.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;

//  TODO: Fix this alongside the `modify_enchantment_level` power type -eggohito
@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

//    @ModifyExpressionValue(method = "forEachEnchantment(Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
//    private static boolean apoli$forEachIsEmpty(boolean original, EnchantmentHelper.Consumer consumer, ItemStack stack) {
//        return original && !ModifyEnchantmentLevelPower.isInEnchantmentMap(((EntityLinkedItemStack) stack).apoli$getEntity());
//    }
//
//    @ModifyExpressionValue(method = "forEachEnchantment(Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getEnchantments()Lnet/minecraft/nbt/NbtList;"))
//    private static NbtList apoli$getEnchantmentsOnForEach(NbtList original, EnchantmentHelper.Consumer consumer, ItemStack stack) {
//        return ModifyEnchantmentLevelPower.getEnchantments(stack, original);
//    }
//
//    @ModifyExpressionValue(method = "getLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
//    private static boolean apoli$getlevelWhenEmpty(boolean original, Enchantment enchantment, ItemStack stack) {
//        return original && !ModifyEnchantmentLevelPower.isInEnchantmentMap(((EntityLinkedItemStack) stack).apoli$getEntity());
//    }
//
//    @ModifyExpressionValue(method = "getLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getEnchantments()Lnet/minecraft/nbt/NbtList;"))
//    private static NbtList apoli$getEnchantmentsOnGetLevel(NbtList original, Enchantment enchantment, ItemStack stack) {
//        return ModifyEnchantmentLevelPower.getEnchantments(stack, original);
//    }
//
//    @ModifyExpressionValue(method = "chooseEquipmentWith(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/entity/LivingEntity;Ljava/util/function/Predicate;)Ljava/util/Map$Entry;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
//    private static boolean apoli$allowEmptyEquipmentChoosing(boolean original, @Local ItemStack stack) {
//        return original && !ModifyEnchantmentLevelPower.isInEnchantmentMap(((EntityLinkedItemStack) stack).apoli$getEntity());
//    }

}
