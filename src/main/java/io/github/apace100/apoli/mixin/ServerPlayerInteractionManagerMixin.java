package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnBlockBreakPower;
import io.github.apace100.apoli.power.ActionOnBlockUsePower;
import io.github.apace100.apoli.power.ModifyHarvestPower;
import io.github.apace100.apoli.power.PreventBlockUsePower;
import io.github.apace100.apoli.util.SavedBlockPosition;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "tryBreakBlock", at = {@At(value = "RETURN", ordinal = 3), @At(value = "RETURN", ordinal = 4, shift = At.Shift.BEFORE)})
    private void apoli$actionOnBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) boolean blockRemoved, @Share("cachedMinedBlock") LocalRef<SavedBlockPosition> cachedMinedBlockRef, @Share("modifiedCanHarvest") LocalBooleanRef modifiedCanHarvestRef) {
        boolean harvestedSuccessfully = blockRemoved && modifiedCanHarvestRef.get();
        PowerHolderComponent.withPowers(this.player, ActionOnBlockBreakPower.class,
            aobbp -> aobbp.doesApply(cachedMinedBlockRef.get()),
            aobbp -> aobbp.executeActions(harvestedSuccessfully, pos, null));
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;shouldCancelInteraction()Z"), cancellable = true)
    private void apoli$preventBlockInteraction(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (PowerHolderComponent.hasPower(player, PreventBlockUsePower.class, pbup -> pbup.doesPrevent(world, hitResult.getBlockPos()))) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"))
    private void apoli$executeBlockUseActions(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        PowerHolderComponent.withPowers(this.player, ActionOnBlockUsePower.class,
            aobbp -> aobbp.shouldExecute(hitResult, hand, stack),
            aobbp -> aobbp.executeAction(hitResult, hand));
    }

}
