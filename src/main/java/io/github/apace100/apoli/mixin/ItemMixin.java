package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ItemOnItemPower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "onClicked", at = @At("RETURN"), cancellable = true)
    private void forgeItem(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValue()) {
            return;
        }
        if (clickType != ClickType.RIGHT) {
            return;
        }
        List<ItemOnItemPower> powers = PowerHolderComponent.getPowers(player, ItemOnItemPower.class).stream().filter(p -> p.doesApply(otherStack, stack)).collect(Collectors.toList());
        for (ItemOnItemPower p :
            powers) {
            p.execute(otherStack, stack, slot.id);
        }
        if(powers.size() > 0) {
            cir.setReturnValue(true);
        }
    }
}
