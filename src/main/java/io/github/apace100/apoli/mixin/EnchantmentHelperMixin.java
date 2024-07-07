package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.power.ModifyEnchantmentLevelPower;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @ModifyVariable(method = "getLevel", at = @At(value = "LOAD"))
    private static ItemEnchantmentsComponent apoli$modifyEnchantmentsForLevel(ItemEnchantmentsComponent original, RegistryEntry<Enchantment> enchantment, ItemStack stack) {
        return ModifyEnchantmentLevelPower.getAndUpdateModifiedEnchantments(stack, original);
    }

    @ModifyVariable(method = "forEachEnchantment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;)V", at = @At(value = "LOAD"))
    private static ItemEnchantmentsComponent apoli$modifyForEachEnchantments(ItemEnchantmentsComponent original, ItemStack stack, EnchantmentHelper.Consumer consumer) {
        return ModifyEnchantmentLevelPower.getAndUpdateModifiedEnchantments(stack, original);
    }

    @ModifyExpressionValue(method = "forEachEnchantment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/enchantment/EnchantmentHelper$ContextAwareConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private static boolean apoli$allowWorkableEmptiesInForEach(boolean original, ItemStack stack, EquipmentSlot slot, LivingEntity entity, EnchantmentHelper.ContextAwareConsumer contextAwareConsumer) {
        return original && !ModifyEnchantmentLevelPower.isWorkableEmptyStack(stack);
    }

    @ModifyVariable(method = "forEachEnchantment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/enchantment/EnchantmentHelper$ContextAwareConsumer;)V", at = @At(value = "LOAD"))
    private static ItemEnchantmentsComponent apoli$modifyForEachContextAwareEnchantments(ItemEnchantmentsComponent original, ItemStack stack, EquipmentSlot slot, LivingEntity entity, EnchantmentHelper.ContextAwareConsumer contextAwareConsumer) {
        return ModifyEnchantmentLevelPower.getAndUpdateModifiedEnchantments(stack, original);
    }

    @ModifyVariable(method = "hasAnyEnchantmentsIn", at = @At(value = "LOAD"))
    private static ItemEnchantmentsComponent apoli$hasEnchantmentsIn(ItemEnchantmentsComponent original, ItemStack stack, TagKey<Enchantment> tag) {
        return ModifyEnchantmentLevelPower.getAndUpdateModifiedEnchantments(stack, original);
    }

}
