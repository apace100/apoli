package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.power.ModifyEnchantmentLevelPower;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
    @Unique
    private ItemStack apoli$capturedItem;

    @Inject(method = "getEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void captureSlotItem(LivingEntity entity, CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir, Map map, EquipmentSlot[] var3, int var4, int var5, EquipmentSlot equipmentSlot, ItemStack stack) {
        this.apoli$capturedItem = stack;
    }

    @Redirect(method = "getEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private boolean allowEmptySlotItemIfModified(ItemStack instance) {
        if (this.apoli$capturedItem != null && this.apoli$capturedItem.isEmpty() && ((EntityLinkedItemStack)apoli$capturedItem).apoli$getEntity() instanceof LivingEntity living && ModifyEnchantmentLevelPower.isInEnchantmentMap(living)) {
            return false;
        }
        return instance.isEmpty();
    }
}
