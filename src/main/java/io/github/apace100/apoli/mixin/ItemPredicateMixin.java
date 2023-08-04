package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.power.ModifyEnchantmentLevelPower;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.predicate.item.ItemPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemPredicate.class)
public class ItemPredicateMixin {

    @Unique
    private ItemStack apugli$itemStack;

    @Inject(method = "test", at = @At("HEAD"))
    private void captureItemStack(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        apugli$itemStack = itemStack;
    }

    @ModifyArg(method = "test", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;fromNbt(Lnet/minecraft/nbt/NbtList;)Ljava/util/Map;"))
    private NbtList getEnchantmentsForEachEnchantment(NbtList original) {
        return ModifyEnchantmentLevelPower.getEnchantments(apugli$itemStack, original);
    }

}
