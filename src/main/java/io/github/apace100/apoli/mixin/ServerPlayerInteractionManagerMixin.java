package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.ActionResultUtil;
import io.github.apace100.apoli.util.BlockUsagePhase;
import io.github.apace100.apoli.util.PriorityPhase;
import io.github.apace100.apoli.util.SavedBlockPosition;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    protected ServerWorld world;

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void apoli$cacheMinedBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Share("cachedMinedBlock") LocalRef<SavedBlockPosition> cachedMinedBlockRef, @Share("modifiedCanHarvest") LocalBooleanRef modifiedCanHarvestRef) {
        cachedMinedBlockRef.set(new SavedBlockPosition(world, pos));
        modifiedCanHarvestRef.set(false);
    }

    @ModifyExpressionValue(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;canHarvest(Lnet/minecraft/block/BlockState;)Z"))
    private boolean apoli$modifyEffectiveTool(boolean original, @Share("cachedMinedBlock") LocalRef<SavedBlockPosition> cachedMinedBlockRef, @Share("modifiedCanHarvest") LocalBooleanRef modifiedCanHarvestRef) {

        boolean result = PowerHolderComponent.getPowers(this.player, ModifyHarvestPower.class)
            .stream()
            .filter(mhp -> mhp.doesApply(cachedMinedBlockRef.get()))
            .max(ModifyHarvestPower::compareTo)
            .map(ModifyHarvestPower::isHarvestAllowed)
            .orElse(original);

        modifiedCanHarvestRef.set(result);
        return result;

    }

    @Unique
    private Direction apoli$blockBreakDirection;

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"))
    private void apoli$cacheBlockBreakDirection(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        this.apoli$blockBreakDirection = direction;
    }

    @Inject(method = "tryBreakBlock", at = {@At(value = "RETURN", ordinal = 3), @At(value = "RETURN", ordinal = 4, shift = At.Shift.BEFORE)})
    private void apoli$actionOnBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) boolean blockRemoved, @Share("cachedMinedBlock") LocalRef<SavedBlockPosition> cachedMinedBlockRef, @Share("modifiedCanHarvest") LocalBooleanRef modifiedCanHarvestRef) {
        boolean harvestedSuccessfully = blockRemoved && modifiedCanHarvestRef.get();
        PowerHolderComponent.withPowers(this.player, ActionOnBlockBreakPower.class,
            aobbp -> aobbp.doesApply(cachedMinedBlockRef.get()),
            aobbp -> aobbp.executeActions(harvestedSuccessfully, pos, apoli$blockBreakDirection));
    }

    @WrapOperation(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult apoli$beforeUseBlock(BlockState state, World world, PlayerEntity player, Hand hand, BlockHitResult hitResult, Operation<ActionResult> original, @Share("zeroPriority$onBlock") LocalRef<ActionResult> zeroPriority$onBlockRef, @Share("zeroPriority$itemOnBlock") LocalRef<ActionResult> zeroPriority$itemOnBlockRef) {

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
                zeroPriority$onBlockRef.set(previousResult);
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

    @ModifyReturnValue(method = "interactBlock", at = @At(value = "RETURN", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;")))
    private ActionResult apoli$afterUseBlock(ActionResult original, ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, @Share("zeroPriority$onBlock") LocalRef<ActionResult> zeroPriority$onBlockRef) {

        ActionResult zeroPriority$onBlock = zeroPriority$onBlockRef.get();
        ActionResult newResult = ActionResult.PASS;

        if (zeroPriority$onBlock != null && zeroPriority$onBlock != ActionResult.PASS) {
            newResult = zeroPriority$onBlock;
        }

        else if (original == ActionResult.PASS) {

            Prioritized.CallInstance<ActiveInteractionPower> aipci = new Prioritized.CallInstance<>();
            aipci.add(player, ActionOnBlockUsePower.class, p -> p.shouldExecute(BlockUsagePhase.BLOCK, PriorityPhase.AFTER, hitResult, hand, stack));

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

    @WrapOperation(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult apoli$beforeItemUseOnBlock(ItemStack stack, ItemUsageContext context, Operation<ActionResult> original, ServerPlayerEntity mPlayer, World mWorld, ItemStack mStack, Hand mHand, BlockHitResult mHitResult, @Share("zeroPriority$itemOnBlock") LocalRef<ActionResult> zeroPriority$itemOnBlockRef) {

        BlockUsagePhase usePhase = BlockUsagePhase.ITEM;
        if (PreventBlockUsePower.doesPrevent(player, usePhase, mHitResult, mStack, mHand)) {
            return ActionResult.FAIL;
        }

        Prioritized.CallInstance<ActiveInteractionPower> aipci = new Prioritized.CallInstance<>();
        aipci.add(player, ActionOnBlockUsePower.class, p -> p.shouldExecute(usePhase, PriorityPhase.BEFORE, mHitResult, mHand, mStack));

        for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

            if (!aipci.hasPowers(i)) {
                continue;
            }

            List<ActiveInteractionPower> aips = aipci.getPowers(i);
            ActionResult previousResult = ActionResult.PASS;

            for (ActiveInteractionPower aip : aips) {

                ActionResult currentResult = aip instanceof ActionOnBlockUsePower aobup
                    ? aobup.executeAction(mHitResult, mHand)
                    : ActionResult.PASS;

                if (ActionResultUtil.shouldOverride(previousResult, currentResult)) {
                    previousResult = currentResult;
                }

            }

            if (i == 0) {
                zeroPriority$itemOnBlockRef.set(previousResult);
                continue;
            }

            if (previousResult == ActionResult.PASS) {
                continue;
            }

            if (previousResult.shouldSwingHand()) {
                player.swingHand(mHand, true);
            }

            return previousResult;

        }

        return original.call(stack, context);

    }

    @ModifyReturnValue(method = "interactBlock", at = @At("RETURN"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getItemCooldownManager()Lnet/minecraft/entity/player/ItemCooldownManager;")))
    private ActionResult apoli$afterItemUseOnBlock(ActionResult original, ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, @Share("zeroPriority$itemOnBlock") LocalRef<ActionResult> zeroPriority$itemOnBlockRef) {

        ActionResult zeroPriority$itemOnBlock = zeroPriority$itemOnBlockRef.get();
        ActionResult newResult = ActionResult.PASS;

        if (zeroPriority$itemOnBlock != null && zeroPriority$itemOnBlock != ActionResult.PASS) {
            newResult = zeroPriority$itemOnBlock;
        }

        else if (original == ActionResult.PASS) {

            Prioritized.CallInstance<ActiveInteractionPower> aipci = new Prioritized.CallInstance<>();
            aipci.add(player, ActionOnBlockUsePower.class, p -> p.shouldExecute(BlockUsagePhase.ITEM, PriorityPhase.AFTER, hitResult, hand, stack));

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
