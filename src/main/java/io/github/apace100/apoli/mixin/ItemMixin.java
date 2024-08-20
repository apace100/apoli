package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModifyFoodPowerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public abstract class ItemMixin {

    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;canConsume(Z)Z"))
    private boolean apoli$makeItemAlwaysEdible(boolean original, World world, PlayerEntity user, Hand hand, @Local ItemStack stackInHand) {
        return original || PowerHolderComponent.hasPowerType(user, ModifyFoodPowerType.class, mfp -> mfp.doesMakeAlwaysEdible() && mfp.doesApply(stackInHand));
    }

}
