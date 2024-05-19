package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.power.EdibleItemPower;
import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FabricItemStack.class)
public interface FabricItemStackMixin {

    @ModifyReturnValue(method = "getFoodComponent", at = @At("RETURN"))
    private FoodComponent apoli$makeItemEdible(FoodComponent original) {
        return EdibleItemPower.get((ItemStack) this)
            .map(EdibleItemPower::getFoodComponent)
            .orElse(original);
    }

}
