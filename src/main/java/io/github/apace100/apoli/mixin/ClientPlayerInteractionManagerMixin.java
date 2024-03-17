package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnBlockUsePower;
import io.github.apace100.apoli.power.PreventBlockUsePower;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Inject(method = "interactBlockInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;shouldCancelInteraction()Z"), cancellable = true)
    private void apoli$preventBlockInteraction(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if(PowerHolderComponent.hasPower(player, PreventBlockUsePower.class, pbup -> pbup.doesPrevent(player.getWorld(), hitResult.getBlockPos()))) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "interactBlockInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;shouldCancelInteraction()Z"), cancellable = true)
    private void apoli$executeBlockUseActions(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {

        ActionResult result = ActionResult.PASS;
        for (ActionOnBlockUsePower aobup : PowerHolderComponent.getPowers(player, ActionOnBlockUsePower.class)) {

            if (!aobup.shouldExecute(hitResult, hand, player.getStackInHand(hand))) {
                continue;
            }

            ActionResult newResult = aobup.executeAction(hitResult, hand);
            if ((newResult.isAccepted() && !result.isAccepted()) || (newResult.shouldSwingHand() && !result.shouldSwingHand())) {
                result = newResult;
            }

        }

        if (result.isAccepted()) {
            cir.setReturnValue(result);
        }

    }

}
