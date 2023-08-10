package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.*;
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

    @ModifyReturnValue(method = "canPlace", at = @At("RETURN"))
    private boolean apoli$preventBlockPlace(boolean original, ItemPlacementContext context) {

        PlayerEntity playerEntity = context.getPlayer();
        if (playerEntity == null) {
            return original;
        }

        BlockPos onPos = ((ItemUsageContextAccessor) context).callGetHitResult().getBlockPos();
        BlockPos toPos = context.getBlockPos();
        Direction direction = context.getSide();

        Prioritized.CallInstance<BlockPlacePower> bppci = new Prioritized.CallInstance<>();
        bppci.add(playerEntity, PreventBlockPlacePower.class, p -> p.doesApply(context.getHand(), onPos, toPos, direction, context.getStack()));

        int preventBlockPlacePowers = 0;
        for (int i = bppci.getMaxPriority(); i >= bppci.getMinPriority(); i--) {

            List<PreventBlockPlacePower> pbpps = bppci.getPowers(i)
                .stream()
                .filter(p -> p instanceof PreventBlockPlacePower)
                .map(p -> (PreventBlockPlacePower) p)
                .toList();

            preventBlockPlacePowers += pbpps.size();
            pbpps.forEach(p -> p.executeOtherActions(onPos, toPos, direction));

        }

        return preventBlockPlacePowers <= 0 && original;

    }

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private void apoli$actionOnBlockPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir, @Local PlayerEntity playerEntity, @Local BlockPos toPos, @Local ItemStack stack, @Share("bppci") LocalRef<Prioritized.CallInstance<BlockPlacePower>> bppciRef) {

        if (playerEntity == null) {
            return;
        }

        BlockPos onPos = ((ItemUsageContextAccessor) context).callGetHitResult().getBlockPos();
        Direction direction = context.getSide();
        Hand hand = context.getHand();

        Prioritized.CallInstance<BlockPlacePower> bppci = new Prioritized.CallInstance<>();
        bppci.add(playerEntity, ActionOnBlockPlacePower.class, p -> p.doesApply(hand, onPos, toPos, direction, stack));

        for (int i = bppci.getMaxPriority(); i >= bppci.getMinPriority(); i--) {
            bppci.getPowers(i).forEach(p -> p.executeOtherActions(onPos, toPos, direction));
        }

        bppciRef.set(bppci);

    }

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At("TAIL"))
    private void apoli$postActionOnBlockPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir, @Share("bppci") LocalRef<Prioritized.CallInstance<BlockPlacePower>> bppciRef) {
        Prioritized.CallInstance<BlockPlacePower> bppci = bppciRef.get();
        for (int i = bppci.getMaxPriority(); i >= bppci.getMinPriority(); i--) {
            bppci.getPowers(i).forEach(p -> p.executeItemAction(context.getHand()));
        }
    }

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

}
