package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.power.ModifyEnchantmentLevelPower;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.predicate.item.ItemPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemPredicate.class)
public class ItemPredicateMixin {

    @ModifyExpressionValue(method = "test", at = {@At(value = "INVOKE", target = "Lnet/minecraft/item/EnchantedBookItem;getEnchantmentNbt(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/nbt/NbtList;"), @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getEnchantments()Lnet/minecraft/nbt/NbtList;")})
    private NbtList apoli$getEnchantments(NbtList original, ItemStack stack) {
        return ModifyEnchantmentLevelPower.getEnchantments(stack, original);
    }

}
