package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.power.ModifyEnchantmentLevelPower;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.item.ComponentSubPredicate;
import net.minecraft.predicate.item.EnchantmentsPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EnchantmentsPredicate.class)
public abstract class EnchantmentsPredicateMixin implements ComponentSubPredicate<ItemEnchantmentsComponent> {
    @ModifyVariable(method = "test(Lnet/minecraft/item/ItemStack;Lnet/minecraft/component/type/ItemEnchantmentsComponent;)Z", at = @At("HEAD"), argsOnly = true)
    private ItemEnchantmentsComponent apoli$modifyEnchantmentsPredicate(ItemEnchantmentsComponent component, ItemStack stack) {
        if (this.getComponentType() == DataComponentTypes.ENCHANTMENTS)
            return ModifyEnchantmentLevelPower.getEnchantments(stack, component, true);
        return component;
    }
}
