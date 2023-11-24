package io.github.apace100.apoli.mixin.integration.appleskin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.EdibleItemPower;
import io.github.apace100.apoli.power.PreventItemUsePower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import squeek.appleskin.helpers.FoodHelper;

import java.util.Comparator;

@Pseudo
@Mixin(FoodHelper.class)
public class FoodHelperMixin {
    @ModifyReturnValue(method = "isFood", at = @At(value = "RETURN", ordinal = 1), remap = false)
    private static boolean apoli$setEdibleItemAsFood(boolean original, ItemStack stack) {
        if (PowerHolderComponent.getPowers(((EntityLinkedItemStack)stack).apoli$getEntity(), EdibleItemPower.class).stream().anyMatch(p -> p.doesApply(stack))) {
            return true;
        }
        return original;
    }

    @ModifyReturnValue(method = "canConsume", at = @At("RETURN"))
    private static boolean apoli$powerCanConsume(boolean original, ItemStack stack, PlayerEntity player) {
        if (PowerHolderComponent.getPowers(((EntityLinkedItemStack)stack).apoli$getEntity(), EdibleItemPower.class).stream().anyMatch(p -> p.doesApply(stack))) {
            return true;
        }
        if (PowerHolderComponent.getPowers(player, PreventItemUsePower.class).stream().anyMatch(p -> p.doesPrevent(stack))) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = "isRotten", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getFoodComponent()Lnet/minecraft/item/FoodComponent;"))
    private static FoodComponent apoli$isEdibleItemPowerRotten(FoodComponent original, ItemStack stack) {
        return PowerHolderComponent.getPowers(((EntityLinkedItemStack)stack).apoli$getEntity(), EdibleItemPower.class)
                .stream()
                .filter(p -> p.doesApply(stack))
                .max(Comparator.comparing(EdibleItemPower::getPriority))
                .map(EdibleItemPower::getFoodComponent).orElse(original);
    }

    @ModifyExpressionValue(method = "getEstimatedHealthIncrement(Lnet/minecraft/item/ItemStack;Lsqueek/appleskin/api/food/FoodValues;Lnet/minecraft/entity/player/PlayerEntity;)F", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getFoodComponent()Lnet/minecraft/item/FoodComponent;"))
    private static FoodComponent apoli$estimateHealthIncrementWithPowerEffects(FoodComponent original, ItemStack stack) {
        return PowerHolderComponent.getPowers(((EntityLinkedItemStack)stack).apoli$getEntity(), EdibleItemPower.class)
                .stream()
                .filter(p -> p.doesApply(stack))
                .max(Comparator.comparing(EdibleItemPower::getPriority))
                .map(EdibleItemPower::getFoodComponent).orElse(original);
    }

}
