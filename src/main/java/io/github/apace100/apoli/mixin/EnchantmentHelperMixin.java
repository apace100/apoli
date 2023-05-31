package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyEnchantmentLevelPower;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Redirect(method = "forEachEnchantment(Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private static boolean forEachIsEmpty(ItemStack instance) {
        if (((EntityLinkedItemStack) instance).getEntity() instanceof LivingEntity living && PowerHolderComponent.hasPower(living, ModifyEnchantmentLevelPower.class)) {
            return false;
        }
        return instance.isEmpty();
    }

    private static ItemStack apugli$runIterationOnItem;

    @Inject(method = "forEachEnchantment(Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "HEAD"))
    private static void getEnchantmentItemStack(EnchantmentHelper.Consumer enchantmentVisitor, ItemStack itemStack, CallbackInfo ci) {
        apugli$runIterationOnItem = itemStack;
    }

    @ModifyVariable(method = "forEachEnchantment(Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemStack;getEnchantments()Lnet/minecraft/nbt/NbtList;"))
    private static NbtList getEnchantmentsForEachEnchantment(NbtList original) {
        return ModifyEnchantmentLevelPower.getEnchantments(apugli$runIterationOnItem, original);
    }

    private static ItemStack apugli$itemEnchantmentLevelStack;

    @Inject(method = "getLevel", at = @At("HEAD"))
    private static void getEnchantmentItemStack(Enchantment enchantment, ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
        apugli$itemEnchantmentLevelStack = itemStack;
    }

    @Redirect(method = "getLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private static boolean getLevelIsEmpty(ItemStack instance) {
        if (((EntityLinkedItemStack) instance).getEntity() instanceof LivingEntity living && PowerHolderComponent.hasPower(living, ModifyEnchantmentLevelPower.class)) {
            return false;
        }
        return instance.isEmpty();
    }

    @ModifyVariable(method = "getLevel", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemStack;getEnchantments()Lnet/minecraft/nbt/NbtList;"))
    private static NbtList getEnchantmentsGetLevel(NbtList original) {
        return ModifyEnchantmentLevelPower.getEnchantments(apugli$itemEnchantmentLevelStack, original);
    }

}
