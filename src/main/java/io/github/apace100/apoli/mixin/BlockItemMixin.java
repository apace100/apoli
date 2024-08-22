package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.*;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @ModifyReturnValue(method = "canPlace", at = @At("RETURN"))
    private boolean apoli$preventBlockPlace(boolean original, ItemPlacementContext context, BlockState state) {

        PlayerEntity playerEntity = context.getPlayer();
        if (playerEntity == null) {
            return original;
        }

        Direction direction = context.getSide();
        ItemStack stack = context.getStack();
        Hand hand = context.getHand();

        BlockPos toPos = context.getBlockPos();
        BlockPos onPos = ((ItemUsageContextAccessor) context).callGetHitResult().getBlockPos();

        Prioritized.CallInstance<ActiveInteractionPowerType> aipci = new Prioritized.CallInstance<>();
        int preventBlockPlacePowers = 0;

        aipci.add(playerEntity, PreventBlockPlacePowerType.class, pbpp -> pbpp.doesPrevent(stack, hand, toPos, onPos, direction));

        for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

            if (!aipci.hasPowerTypes(i)) {
                continue;
            }

            List<PreventBlockPlacePowerType> pbpps = aipci.getPowerTypes(i)
                .stream()
                .filter(p -> p instanceof PreventBlockPlacePowerType)
                .map(p -> (PreventBlockPlacePowerType) p)
                .toList();

            preventBlockPlacePowers += pbpps.size();
            pbpps.forEach(pbpp -> pbpp.executeActions(hand, toPos, onPos, direction));

        }

        return preventBlockPlacePowers <= 0 && original;

    }

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private void apoli$actionOnBlockPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir, @Local PlayerEntity user, @Local BlockPos toPos, @Local ItemStack stack, @Share("aipci") LocalRef<Prioritized.CallInstance<ActiveInteractionPowerType>> aipciRef) {

        if (user == null) {
            return;
        }

        Direction direction = context.getSide();
        BlockPos onPos = ((ItemUsageContextAccessor) context).callGetHitResult().getBlockPos();
        Hand hand = context.getHand();

        Prioritized.CallInstance<ActiveInteractionPowerType> aipci = new Prioritized.CallInstance<>();
        aipci.add(user, ActionOnBlockPlacePowerType.class, aobpp -> aobpp.shouldExecute(stack, hand, toPos, onPos, direction));

        for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {
            aipci.getPowerTypes(i)
                .stream()
                .filter(p -> p instanceof ActionOnBlockPlacePowerType)
                .forEach(p -> ((ActionOnBlockPlacePowerType) p).executeOtherActions(toPos, onPos, direction));
        }

        aipciRef.set(aipci);

    }

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At("TAIL"))
    private void apoli$actionOnBlockPlacePost(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir, @Share("aipci") LocalRef<Prioritized.CallInstance<ActiveInteractionPowerType>> aipciRef) {

        Prioritized.CallInstance<ActiveInteractionPowerType> aipci = aipciRef.get();

        for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {
            aipci.getPowerTypes(i)
                .stream()
                .filter(p -> p instanceof ActionOnBlockPlacePowerType)
                .forEach(p -> ((ActionOnBlockPlacePowerType) p).executeItemActions(context.getHand()));
        }

    }

    @WrapOperation(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"))
    private TypedActionResult<ItemStack> apoli$preventItemUseIfFoodBlockItem(BlockItem instance, World world, PlayerEntity user, Hand hand, Operation<TypedActionResult<ItemStack>> original) {
        ItemStack handStack = user.getStackInHand(hand);
        return PowerHolderComponent.hasPowerType(user, PreventItemUsePowerType.class, p -> p.doesPrevent(handStack))
            ? TypedActionResult.fail(handStack)
            : original.call(instance, world, user, hand);
    }

}
