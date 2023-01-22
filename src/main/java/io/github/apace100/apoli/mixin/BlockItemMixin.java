package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnBlockPlacePower;
import io.github.apace100.apoli.power.ActiveInteractionPower;
import io.github.apace100.apoli.power.PreventBlockPlacePower;
import io.github.apace100.apoli.power.PreventItemUsePower;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BlockItem;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"), method = "useOnBlock")
    private TypedActionResult<ItemStack> preventItemUseIfBlockItem(BlockItem blockItem, World world, PlayerEntity user, Hand hand) {
        if(user != null) {
            PowerHolderComponent component = PowerHolderComponent.KEY.get(user);
            ItemStack stackInHand = user.getStackInHand(hand);
            for(PreventItemUsePower piup : component.getPowers(PreventItemUsePower.class)) {
                if(piup.doesPrevent(stackInHand)) {
                    return TypedActionResult.fail(stackInHand);
                }
            }
        }
        return blockItem.use(world, user, hand);
    }

    @Inject(method = "canPlace", at = @At("HEAD"), cancellable = true)
    private void preventBlockPlace(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity playerEntity = context.getPlayer();
        ItemStack itemStack = context.getStack();
        BlockPos hitPos = ((ItemUsageContextAccessor) context).callGetHitResult().getBlockPos();
        BlockPos placementPos = context.getBlockPos();
        Direction direction = context.getSide();
        Hand hand = context.getHand();

        if (playerEntity == null) return;

        int preventBlockPlacePowers = 0;
        ActiveInteractionPower.CallInstance<PreventBlockPlacePower> pbppci = new ActiveInteractionPower.CallInstance<>();
        pbppci.add(playerEntity, PreventBlockPlacePower.class, p -> p.doesPrevent(hand, hitPos, placementPos, direction, itemStack));

        for (int i = pbppci.getMaxPriority(); i >= 0; i--) {
            if (!pbppci.hasPowers(i)) continue;

            List<PreventBlockPlacePower> pbpps = pbppci.getPowers(i);
            preventBlockPlacePowers += pbpps.size();

            pbpps.forEach(pbpp -> pbpp.executeActions(hand, hitPos, placementPos, direction));
        }

        if (preventBlockPlacePowers > 0) cir.setReturnValue(false);
    }

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemPlacementContext;getBlockPos()Lnet/minecraft/util/math/BlockPos;"), cancellable = true)
    private void actionOnBlockPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity playerEntity = context.getPlayer();
        ItemStack itemStack = context.getStack();
        BlockPos hitPos = ((ItemUsageContextAccessor) context).callGetHitResult().getBlockPos();
        BlockPos placementPos = context.getBlockPos();
        Direction direction = context.getSide();
        Hand hand = context.getHand();

        if (playerEntity == null) return;

        ActiveInteractionPower.CallInstance<ActionOnBlockPlacePower> aobppci = new ActiveInteractionPower.CallInstance<>();
        aobppci.add(playerEntity, ActionOnBlockPlacePower.class, aobpp -> aobpp.shouldExecute(hand, hitPos, placementPos , direction, itemStack));

        for (int i = aobppci.getMaxPriority(); i >= 0; i--) {
            if (!aobppci.hasPowers(i)) continue;

            List<ActionOnBlockPlacePower> aobpps = aobppci.getPowers(i);
            aobpps.forEach(aobpp -> aobpp.executeActions(hand, hitPos, placementPos, direction));
        }
    }

}
