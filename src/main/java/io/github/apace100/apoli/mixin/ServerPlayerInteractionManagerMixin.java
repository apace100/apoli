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
import io.github.apace100.apoli.power.type.*;
import io.github.apace100.apoli.util.ActionResultUtil;
import io.github.apace100.apoli.util.BlockUsagePhase;
import io.github.apace100.apoli.util.PriorityPhase;
import io.github.apace100.apoli.util.SavedBlockPosition;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
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
import java.util.WeakHashMap;

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

        boolean result = PowerHolderComponent.getPowerTypes(this.player, ModifyHarvestPowerType.class)
            .stream()
            .filter(mhp -> mhp.doesApply(cachedMinedBlockRef.get()))
            .max(ModifyHarvestPowerType::compareTo)
            .map(ModifyHarvestPowerType::isHarvestAllowed)
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
        PowerHolderComponent.withPowerTypes(this.player, ActionOnBlockBreakPowerType.class,
            aobbp -> aobbp.doesApply(cachedMinedBlockRef.get()),
            aobbp -> aobbp.executeActions(harvestedSuccessfully, pos, apoli$blockBreakDirection));
    }

    @WrapOperation(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult apoli$beforeUseBlock(BlockState state, World world, PlayerEntity player, BlockHitResult hitResult, Operation<ActionResult> original, ServerPlayerEntity mPlayer, World mWorld, ItemStack mStack, Hand mHand, @Share("zeroPriority$onBlock") LocalRef<ActionResult> zeroPriority$onBlockRef, @Share("zeroPriority$itemOnBlock") LocalRef<ActionResult> zeroPriority$itemOnBlockRef) {

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
                zeroPriority$onBlockRef.set(previousResult);
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

    @ModifyReturnValue(method = "interactBlock", at = @At(value = "RETURN", ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;")))
    private ActionResult apoli$afterUseBlock(ActionResult original, ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, @Share("zeroPriority$onBlock") LocalRef<ActionResult> zeroPriority$onBlockRef) {

        ActionResult zeroPriority$onBlock = zeroPriority$onBlockRef.get();
        ActionResult newResult = ActionResult.PASS;

        if (zeroPriority$onBlock != null && zeroPriority$onBlock != ActionResult.PASS) {
            newResult = zeroPriority$onBlock;
        }

        else if (original == ActionResult.PASS) {

            Prioritized.CallInstance<ActiveInteractionPowerType> aipci = new Prioritized.CallInstance<>();
            aipci.add(player, ActionOnBlockUsePowerType.class, p -> p.shouldExecute(BlockUsagePhase.BLOCK, PriorityPhase.AFTER, hitResult, hand, stack));

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

    @WrapOperation(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUseWithItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ItemActionResult;"))
    private ItemActionResult apoli$beforeItemUseOnBlock(BlockState state, ItemStack stack, World world, PlayerEntity player, Hand hand, BlockHitResult hitResult, Operation<ItemActionResult> original, @Share("zeroPriority$itemOnBlock") LocalRef<ActionResult> zeroPriority$itemOnBlockRef) {

        BlockUsagePhase usePhase = BlockUsagePhase.ITEM;
        if (PreventBlockUsePowerType.doesPrevent(player, usePhase, hitResult, stack, hand)) {
            return ItemActionResult.FAIL;
        }

        Prioritized.CallInstance<ActiveInteractionPowerType> aipci = new Prioritized.CallInstance<>();
        aipci.add(player, ActionOnBlockUsePowerType.class, p -> p.shouldExecute(usePhase, PriorityPhase.BEFORE, hitResult, hand, stack));

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
                zeroPriority$itemOnBlockRef.set(previousResult);
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

    @ModifyReturnValue(method = "interactBlock", at = @At("RETURN"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/util/ItemActionResult;isAccepted()Z")))
    private ActionResult apoli$afterItemUseOnBlock(ActionResult original, ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, @Share("zeroPriority$itemOnBlock") LocalRef<ActionResult> zeroPriority$itemOnBlockRef) {

        ActionResult zeroPriority$itemOnBlock = zeroPriority$itemOnBlockRef.get();
        ActionResult newResult = ActionResult.PASS;

        if (zeroPriority$itemOnBlock != null && zeroPriority$itemOnBlock != ActionResult.PASS) {
            newResult = zeroPriority$itemOnBlock;
        }

        else if (original == ActionResult.PASS) {

            Prioritized.CallInstance<ActiveInteractionPowerType> aipci = new Prioritized.CallInstance<>();
            aipci.add(player, ActionOnBlockUsePowerType.class, p -> p.shouldExecute(BlockUsagePhase.ITEM, PriorityPhase.AFTER, hitResult, hand, stack));

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

    @Inject(method = "tryBreakBlock", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER))
    private void apoli$cacheCopyAndOriginalStacks(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) ItemStack originalStack, @Local(ordinal = 1) ItemStack copyStack) {
        ModifyEnchantmentLevelPowerType.COPY_TO_ORIGINAL_STACK
            .computeIfAbsent(player.getUuid(), k -> new WeakHashMap<>())
            .put(copyStack, originalStack);
    }

    @Inject(method = "tryBreakBlock", at = @At("RETURN"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;")))
    private void apoli$clearCachedCopyAndOriginalStacks(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ModifyEnchantmentLevelPowerType.COPY_TO_ORIGINAL_STACK.remove(player.getUuid());
    }

}
