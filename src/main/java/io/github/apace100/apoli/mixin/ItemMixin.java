package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ItemOnItemPower;
import io.github.apace100.apoli.power.ModifyFoodPower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.FoodComponent;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(Item.class)
public class ItemMixin {

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/FoodComponent;isAlwaysEdible()Z"))
    private boolean makeItemEdible(FoodComponent foodComponent, World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if(PowerHolderComponent.KEY.get(user).getPowers(ModifyFoodPower.class).stream()
            .anyMatch(p -> p.doesMakeAlwaysEdible() && p.doesApply(itemStack))) {
            return true;
        }
        return foodComponent.isAlwaysEdible();
    }

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
            p.execute(otherStack, stack, slot);
        }
        if(powers.size() > 0) {
            cir.setReturnValue(true);
        }
    }
}
