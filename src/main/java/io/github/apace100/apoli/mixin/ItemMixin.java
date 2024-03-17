package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ItemOnItemPower;
import io.github.apace100.apoli.power.ModifyFoodPower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Item.class)
public abstract class ItemMixin {

    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;canConsume(Z)Z"))
    private boolean apoli$makeItemAlwaysEdible(boolean original, World world, PlayerEntity user, Hand hand, @Local ItemStack stackInHand) {
        return original || PowerHolderComponent.hasPower(user, ModifyFoodPower.class, mfp -> mfp.doesMakeAlwaysEdible() && mfp.doesApply(stackInHand));
    }

    @Inject(method = "onClicked", at = @At("RETURN"), cancellable = true)
    private void apoli$itemOnItem(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference, CallbackInfoReturnable<Boolean> cir) {

        if (cir.getReturnValue()) {
            return;
        }

        List<ItemOnItemPower> itemOnItemPowers = PowerHolderComponent
            .getPowers(player, ItemOnItemPower.class)
            .stream()
            .filter(p -> p.doesApply(otherStack, stack, clickType))
            .toList();

        if (!itemOnItemPowers.isEmpty()) {
            itemOnItemPowers.forEach(p -> p.execute(cursorStackReference, StackReference.of(slot.inventory, slot.getIndex()), slot));
            cir.setReturnValue(true);
        }

    }

}
