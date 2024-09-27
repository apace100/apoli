package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.power.type.ActionOnBlockUsePowerType;
import io.github.apace100.apoli.power.type.ActiveInteractionPowerType;
import io.github.apace100.apoli.power.type.PreventBlockUsePowerType;
import io.github.apace100.apoli.power.type.Prioritized;
import io.github.apace100.apoli.util.ActionResultUtil;
import io.github.apace100.apoli.util.BlockUsagePhase;
import io.github.apace100.apoli.util.PriorityPhase;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.List;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @WrapOperation(method = "interactBlockInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult apoli$beforeUseBlock(BlockState state, World world, PlayerEntity player, BlockHitResult hitResult, Operation<ActionResult> original, ClientPlayerEntity mPlayer, Hand mHand, @Share("zeroPriority$useBlock") LocalRef<ActionResult> zeroPriority$useBlockRef) {

        ItemStack stackInHand = player.getStackInHand(mHand);
        BlockUsagePhase usePhase = BlockUsagePhase.BLOCK;

        if (PreventBlockUsePowerType.doesPrevent(player, usePhase, hitResult, stackInHand, mHand)) {
            return ActionResult.FAIL;
        }

        Prioritized.CallInstance<ActiveInteractionPowerType> aipci = new Prioritized.CallInstance<>();
        aipci.add(player, ActionOnBlockUsePowerType.class, p -> p.shouldExecute(usePhase, PriorityPhase.BEFORE, hitResult, mHand, stackInHand));

        for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

            if (!aipci.hasPowerTypes(i)) {
                continue;
            }

            List<ActiveInteractionPowerType> aips = aipci.getPowerTypes(i);
            ActionResult previousResult = ActionResult.PASS;

            for (ActiveInteractionPowerType aip : aips) {

                ActionResult currentResult = aip instanceof ActionOnBlockUsePowerType aobup
                    ? aobup.executeAction(hitResult, mHand)
                    : ActionResult.PASS;

                if (ActionResultUtil.shouldOverride(previousResult, currentResult)) {
                    previousResult = currentResult;
                }

            }

            if (i == 0) {
                zeroPriority$useBlockRef.set(previousResult);
                continue;
            }

            if (previousResult == ActionResult.PASS) {
                continue;
            }

            if (previousResult.shouldSwingHand()) {
                player.swingHand(mHand);
            }

            return previousResult;

        }

        return original.call(state, world, player, hitResult);

    }

    @ModifyReturnValue(method = "interactBlockInternal", at = @At(value = "RETURN", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/util/ActionResult;isAccepted()Z")))
    private ActionResult apoli$afterUseBlock(ActionResult original, ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, @Share("zeroPriority$useBlock") LocalRef<ActionResult> zeroPriority$useBlockRef) {

        ItemStack stackInHand = player.getStackInHand(hand);

        ActionResult zeroPriority$useBlock = zeroPriority$useBlockRef.get();
        ActionResult newResult = ActionResult.PASS;

        if (zeroPriority$useBlock != null && zeroPriority$useBlock != ActionResult.PASS) {
            newResult = zeroPriority$useBlock;
        }

        else if (original == ActionResult.PASS) {

            Prioritized.CallInstance<ActiveInteractionPowerType> aipci = new Prioritized.CallInstance<>();
            aipci.add(player, ActionOnBlockUsePowerType.class, p -> p.shouldExecute(BlockUsagePhase.BLOCK, PriorityPhase.AFTER, hitResult, hand, stackInHand));

            for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

                if (!aipci.hasPowerTypes(i)) {
                    continue;
                }

                List<ActiveInteractionPowerType> aips = aipci.getPowerTypes(i);
                ActionResult previousResult = ActionResult.PASS;

                for (ActiveInteractionPowerType aip : aips) {

                    ActionResult currentResult = aip instanceof ActionOnBlockUsePowerType aobup
                        ? aobup.executeAction(hitResult, hand)
                        : ActionResult.PASS;

                    if (ActionResultUtil.shouldOverride(previousResult, currentResult)) {
                        previousResult = currentResult;
                    }

                }

                if (previousResult != ActionResult.PASS) {
                    newResult = previousResult;
                    break;
                }

            }

        }

        if (newResult.shouldSwingHand()) {
            player.swingHand(hand);
        }

        return ActionResultUtil.shouldOverride(original, newResult)
            ? newResult
            : original;

    }

    @WrapOperation(method = "interactBlockInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUseWithItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ItemActionResult;"))
    private ItemActionResult apoli$beforeItemUseOnBlock(BlockState state, ItemStack stack, World world, PlayerEntity player, Hand hand, BlockHitResult hitResult, Operation<ItemActionResult> original, @Share("zeroPriority$itemUseOnBlock") LocalRef<ActionResult> zeroPriority$itemUseOnBlockRef) {

        ItemStack stackInHand = player.getStackInHand(hand);
        BlockUsagePhase usePhase = BlockUsagePhase.ITEM;

        if (PreventBlockUsePowerType.doesPrevent(player, usePhase, hitResult, stackInHand, hand)) {
            return ItemActionResult.FAIL;
        }

        Prioritized.CallInstance<ActiveInteractionPowerType> aipci = new Prioritized.CallInstance<>();
        aipci.add(player, ActionOnBlockUsePowerType.class, p -> p.shouldExecute(usePhase, PriorityPhase.BEFORE, hitResult, hand, stackInHand));

        for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

            if (!aipci.hasPowerTypes(i)) {
                continue;
            }

            List<ActiveInteractionPowerType> aips = aipci.getPowerTypes(i);
            ActionResult previousResult = ActionResult.PASS;

            for (ActiveInteractionPowerType aip : aips) {

                ActionResult currentResult = aip instanceof ActionOnBlockUsePowerType aobup
                    ? aobup.executeAction(hitResult, hand)
                    : ActionResult.PASS;

                if (ActionResultUtil.shouldOverride(previousResult, currentResult)) {
                    previousResult = currentResult;
                }

            }

            if (i == 0) {
                zeroPriority$itemUseOnBlockRef.set(previousResult);
                continue;
            }

            if (previousResult == ActionResult.PASS) {
                continue;
            }

            if (previousResult.shouldSwingHand()) {
                player.swingHand(hand);
            }

            return switch (previousResult) {
                case SUCCESS, SUCCESS_NO_ITEM_USED ->
                    ItemActionResult.SUCCESS;
                case CONSUME ->
                    ItemActionResult.CONSUME;
                case CONSUME_PARTIAL ->
                    ItemActionResult.CONSUME_PARTIAL;
                case FAIL ->
                    ItemActionResult.FAIL;
                default ->
                    throw new IllegalStateException("Unexpected value: " + previousResult);
            };

        }

        return original.call(state, stack, world, player, hand, hitResult);

    }

    @ModifyReturnValue(method = "interactBlockInternal", at = @At(value = "RETURN", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/util/ItemActionResult;isAccepted()Z")))
    private ActionResult apoli$afterItemUseOnBlock(ActionResult original, ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, @Share("zeroPriority$itemUseOnBlock") LocalRef<ActionResult> zeroPriority$itemUseOnBlockRef) {

        ActionResult zeroPriority$itemUseOnBlock = zeroPriority$itemUseOnBlockRef.get();
        ActionResult newResult = ActionResult.PASS;

        if (zeroPriority$itemUseOnBlock != null && zeroPriority$itemUseOnBlock != ActionResult.PASS) {
            newResult = zeroPriority$itemUseOnBlock;
        }

        else if (original == ActionResult.PASS) {

            Prioritized.CallInstance<ActiveInteractionPowerType> aipci = new Prioritized.CallInstance<>();
            aipci.add(player, ActionOnBlockUsePowerType.class, p -> p.shouldExecute(BlockUsagePhase.ITEM, PriorityPhase.AFTER, hitResult, hand, player.getStackInHand(hand)));

            for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

                if (!aipci.hasPowerTypes(i)) {
                    continue;
                }

                List<ActiveInteractionPowerType> aips = aipci.getPowerTypes(i);
                ActionResult previousResult = ActionResult.PASS;

                for (ActiveInteractionPowerType aip : aips) {

                    ActionResult currentResult = aip instanceof ActionOnBlockUsePowerType aobup
                        ? aobup.executeAction(hitResult, hand)
                        : ActionResult.PASS;

                    if (ActionResultUtil.shouldOverride(previousResult, currentResult)) {
                        previousResult = currentResult;
                    }

                }

                if (previousResult != ActionResult.PASS) {
                    newResult = previousResult;
                    break;
                }

            }

        }

        if (newResult.shouldSwingHand()) {
            player.swingHand(hand);
        }

        return ActionResultUtil.shouldOverride(original, newResult)
            ? newResult
            : original;

    }

}
