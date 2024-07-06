package io.github.apace100.apoli.mixin.integration.appleskin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.power.EdibleItemPower;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import squeek.appleskin.helpers.FoodHelper;

@Pseudo
@Mixin(FoodHelper.class)
public class FoodHelperMixin {

    @ModifyReturnValue(method = "isFood", at = @At("RETURN"))
    private static boolean apoli$accountForPowerFood(boolean original, ItemStack stack) {
        return original
            || EdibleItemPower.get(stack).isPresent();
    }

}
