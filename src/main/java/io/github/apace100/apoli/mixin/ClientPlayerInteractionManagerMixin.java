package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.power.ActionOnBlockUsePower;
import io.github.apace100.apoli.power.ActiveInteractionPower;
import io.github.apace100.apoli.power.PreventBlockUsePower;
import io.github.apace100.apoli.power.Prioritized;
import io.github.apace100.apoli.util.ActionResultUtil;
import io.github.apace100.apoli.util.BlockUsagePhase;
import io.github.apace100.apoli.util.PriorityPhase;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.List;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @WrapOperation(method = "interactBlockInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult apoli$beforeUseBlock(BlockState state, World world, PlayerEntity player, Hand hand, BlockHitResult hitResult, Operation<ActionResult> original, @Share("zeroPriority$useBlock") LocalRef<ActionResult> zeroPriority$useBlockRef) {

        ItemStack stackInHand = player.getStackInHand(hand);
        BlockUsagePhase usePhase = BlockUsagePhase.BLOCK;

        if (PreventBlockUsePower.doesPrevent(player, usePhase, hitResult, stackInHand, hand)) {
            return ActionResult.FAIL;
        }

        Prioritized.CallInstance<ActiveInteractionPower> aipci = new Prioritized.CallInstance<>();
        aipci.add(player, ActionOnBlockUsePower.class, p -> p.shouldExecute(usePhase, PriorityPhase.BEFORE, hitResult, hand, stackInHand));

        for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

            if (!aipci.hasPowers(i)) {
                continue;
            }

            List<ActiveInteractionPower> aips = aipci.getPowers(i);
            ActionResult previousResult = ActionResult.PASS;

            for (ActiveInteractionPower aip : aips) {

                ActionResult currentResult = aip instanceof ActionOnBlockUsePower aobup
                    ? aobup.executeAction(hitResult, hand)
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
                player.swingHand(hand);
            }

            return previousResult;

        }

        return original.call(state, world, player, hand, hitResult);

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

            Prioritized.CallInstance<ActiveInteractionPower> aipci = new Prioritized.CallInstance<>();
            aipci.add(player, ActionOnBlockUsePower.class, p -> p.shouldExecute(BlockUsagePhase.BLOCK, PriorityPhase.AFTER, hitResult, hand, stackInHand));

            for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

                if (!aipci.hasPowers(i)) {
                    continue;
                }

                List<ActiveInteractionPower> aips = aipci.getPowers(i);
                ActionResult previousResult = ActionResult.PASS;

                for (ActiveInteractionPower aip : aips) {

                    ActionResult currentResult = aip instanceof ActionOnBlockUsePower aobup
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
            player.swingHand(hand, true);
        }

        return ActionResultUtil.shouldOverride(original, newResult)
            ? newResult
            : original;

    }

    @WrapOperation(method = "interactBlockInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult apoli$beforeItemUseOnBlock(ItemStack stack, ItemUsageContext context, Operation<ActionResult> original, ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, @Share("zeroPriority$itemUseOnBlock") LocalRef<ActionResult> zeroPriority$itemUseOnBlockRef) {

        ItemStack stackInHand = player.getStackInHand(hand);
        BlockUsagePhase usePhase = BlockUsagePhase.ITEM;

        if (PreventBlockUsePower.doesPrevent(player, usePhase, hitResult, stackInHand, hand)) {
            return ActionResult.FAIL;
        }

        Prioritized.CallInstance<ActiveInteractionPower> aipci = new Prioritized.CallInstance<>();
        aipci.add(player, ActionOnBlockUsePower.class, p -> p.shouldExecute(usePhase, PriorityPhase.BEFORE, hitResult, hand, stackInHand));

        for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

            if (!aipci.hasPowers(i)) {
                continue;
            }

            List<ActiveInteractionPower> aips = aipci.getPowers(i);
            ActionResult previousResult = ActionResult.PASS;

            for (ActiveInteractionPower aip : aips) {

                ActionResult currentResult = aip instanceof ActionOnBlockUsePower aobup
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

            return previousResult;

        }

        return original.call(stack, context);

    }

    @ModifyReturnValue(method = "interactBlockInternal", at = @At("RETURN"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getItemCooldownManager()Lnet/minecraft/entity/player/ItemCooldownManager;")))
    private ActionResult apoli$afterItemUseOnBlock(ActionResult original, ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, @Share("zeroPriority$itemUseOnBlock") LocalRef<ActionResult> zeroPriority$itemUseOnBlockRef) {

        ActionResult zeroPriority$itemUseOnBlock = zeroPriority$itemUseOnBlockRef.get();
        ActionResult newResult = ActionResult.PASS;

        if (zeroPriority$itemUseOnBlock != null && zeroPriority$itemUseOnBlock != ActionResult.PASS) {
            newResult = zeroPriority$itemUseOnBlock;
        }

        else if (original == ActionResult.PASS) {

            Prioritized.CallInstance<ActiveInteractionPower> aipci = new Prioritized.CallInstance<>();
            aipci.add(player, ActionOnBlockUsePower.class, p -> p.shouldExecute(BlockUsagePhase.ITEM, PriorityPhase.AFTER, hitResult, hand, player.getStackInHand(hand)));

            for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

                if (!aipci.hasPowers(i)) {
                    continue;
                }

                List<ActiveInteractionPower> aips = aipci.getPowers(i);
                ActionResult previousResult = ActionResult.PASS;

                for (ActiveInteractionPower aip : aips) {

                    ActionResult currentResult = aip instanceof ActionOnBlockUsePower aobup
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
