package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.power.ModifyEnchantmentLevelPower;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @ModifyExpressionValue(method = "forEachEnchantment(Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private static boolean forEachIsEmpty(boolean original, EnchantmentHelper.Consumer consumer, ItemStack stack) {
        return original || !(((EntityLinkedItemStack) stack).apoli$getEntity() instanceof LivingEntity living && ModifyEnchantmentLevelPower.isInEnchantmentMap(living));
    }

    @ModifyVariable(method = "forEachEnchantment(Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemStack;getEnchantments()Lnet/minecraft/nbt/NbtList;"))
    private static NbtList getEnchantmentsForEachEnchantment(NbtList original, EnchantmentHelper.Consumer consumer, ItemStack stack) {
        return ModifyEnchantmentLevelPower.getEnchantments(stack, original);
    }

    @ModifyExpressionValue(method = "getLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private static boolean getLevelIsEmpty(boolean original, Enchantment enchantment, ItemStack stack) {
        return original || !(((EntityLinkedItemStack) stack).apoli$getEntity() instanceof LivingEntity living && ModifyEnchantmentLevelPower.isInEnchantmentMap(living));
    }

    @ModifyVariable(method = "getLevel", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemStack;getEnchantments()Lnet/minecraft/nbt/NbtList;"))
    private static NbtList getEnchantmentsGetLevel(NbtList original, Enchantment enchantment, ItemStack stack) {
        return ModifyEnchantmentLevelPower.getEnchantments(stack, original);
    }

    @ModifyExpressionValue(method = "chooseEquipmentWith(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/entity/LivingEntity;Ljava/util/function/Predicate;)Ljava/util/Map$Entry;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private static boolean allowEmptyEquipmentChoosing(boolean original, @Local ItemStack stack) {
        return original || !(((EntityLinkedItemStack) stack).apoli$getEntity() instanceof LivingEntity living && ModifyEnchantmentLevelPower.isInEnchantmentMap(living));
    }

}
