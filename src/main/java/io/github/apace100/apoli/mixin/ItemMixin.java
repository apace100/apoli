package io.github.apace100.apoli.mixin;

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
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/TypedActionResult;fail(Ljava/lang/Object;)Lnet/minecraft/util/TypedActionResult;"), cancellable = true)
    private void tryItemAlwaysEdible(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (PowerHolderComponent.KEY.get(user).getPowers(ModifyFoodPower.class).stream()
            .anyMatch(p -> p.doesMakeAlwaysEdible() && p.doesApply(itemStack))) {
            user.setCurrentHand(hand);
            cir.setReturnValue(TypedActionResult.consume(itemStack));
        }
    }

    @Inject(method = "onClicked", at = @At("RETURN"), cancellable = true)
    private void apoli$itemOnItem(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference, CallbackInfoReturnable<Boolean> cir) {

        if(cir.getReturnValue()) {
            return;
        }

        List<ItemOnItemPower> itemOnItemPowers = PowerHolderComponent
            .getPowers(player, ItemOnItemPower.class)
            .stream()
            .filter(p -> p.doesApply(otherStack, stack, clickType))
            .toList();

        if(!itemOnItemPowers.isEmpty()) {
            itemOnItemPowers.forEach(p -> p.execute(otherStack, stack, slot));
            cir.setReturnValue(true);
        }

    }
}
