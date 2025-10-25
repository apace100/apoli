package io.github.apace100.apoli.mixin.integration.connector;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnBlockBreakPower;
import io.github.apace100.apoli.power.ActionOnBlockUsePower;
import io.github.apace100.apoli.power.PreventBlockUsePower;
import io.github.apace100.apoli.util.HarvestContext;
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
    @Final
    @Shadow
    protected ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void cacheBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        HarvestContext.setBlockPosition(world, pos);
        HarvestContext.setCanHarvest(player.canHarvest(world.getBlockState(pos)));
    }

    @Inject(method = "tryBreakBlock", at = @At("RETURN"))
    private void origins$onBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;

        boolean harvested = HarvestContext.getCanHarvest();
        PowerHolderComponent.getPowers(player, ActionOnBlockBreakPower.class)
                .stream()
                .filter(p -> p.doesApply(HarvestContext.getBlockPosition()))
                .forEach(p -> p.executeActions(harvested, pos, null));
        HarvestContext.clearCanHarvest();
        HarvestContext.clearBlockPosition();
    }


    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;shouldCancelInteraction()Z"), cancellable = true)
    private void preventBlockInteraction(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if(PowerHolderComponent.getPowers(player, PreventBlockUsePower.class).stream().anyMatch(p -> p.doesPrevent(world, hitResult.getBlockPos()))) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void executeBlockUseActions(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        PowerHolderComponent.getPowers(player, ActionOnBlockUsePower.class).stream()
                .filter(p -> p.shouldExecute(hitResult.getBlockPos(), hitResult.getSide(), hand, stack))
                .forEach(p -> p.executeAction(hitResult.getBlockPos(), hitResult.getSide(), hand));
    }
}
