package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PreventItemUsePower;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    //  Prevents the player from 'using' a block item on a block
    @Inject(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BlockItem;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"), cancellable = true)
    private void preventBlockItemUse(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity playerEntity = context.getPlayer();
        ItemStack itemStack = context.getStack();
        if (playerEntity != null) {
            PowerHolderComponent phc = PowerHolderComponent.KEY.get(playerEntity);
            if (phc.getPowers(PreventItemUsePower.class).stream().anyMatch(piup -> piup.doesPreventUsage(itemStack))) {
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }

    //  Prevents the player from placing a block
    @Inject(method = "canPlace", at = @At("HEAD"), cancellable = true)
    private void preventBlockItemPlace(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity playerEntity = context.getPlayer();
        ItemStack itemStack = context.getStack();
        if (playerEntity != null) {
            PowerHolderComponent phc = PowerHolderComponent.KEY.get(playerEntity);
            if (phc.getPowers(PreventItemUsePower.class).stream().anyMatch(piup -> piup.doesPreventPlacement(itemStack))) {
                cir.setReturnValue(false);
            }
        }
    }
}
