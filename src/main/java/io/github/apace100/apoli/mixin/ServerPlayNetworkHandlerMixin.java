package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.EndRespawningEntity;
import io.github.apace100.apoli.power.ActionOnItemUsePower;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onClientStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;respawnPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;Z)Lnet/minecraft/server/network/ServerPlayerEntity;", ordinal = 0))
    private void saveEndRespawnStatus(ClientStatusC2SPacket packet, CallbackInfo ci) {
        ((EndRespawningEntity)this.player).apoli$setEndRespawning(true);
    }

    @Inject(method = "onClientStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/ChangedDimensionCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/registry/RegistryKey;)V"))
    private void undoEndRespawnStatus(ClientStatusC2SPacket packet, CallbackInfo ci) {
        ((EndRespawningEntity)this.player).apoli$setEndRespawning(false);
    }

    @Inject(method = "onUpdateSelectedSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/UpdateSelectedSlotC2SPacket;getSelectedSlot()I", ordinal = 0))
    private void callActionOnUseStopBySwitching(UpdateSelectedSlotC2SPacket packet, CallbackInfo ci) {
        if(player.isUsingItem()) {
            ActionOnItemUsePower.executeActions(player, player.getActiveItem(), player.getActiveItem(), ActionOnItemUsePower.TriggerType.STOP, ActionOnItemUsePower.PriorityPhase.ALL);
        }
    }

    @Inject(method = "onPlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;clearActiveItem()V"))
    private void callActionOnUseStopBySwappingHands(PlayerActionC2SPacket packet, CallbackInfo ci) {
        if(player.isUsingItem()) {
            ActionOnItemUsePower.executeActions(player, player.getActiveItem(), player.getActiveItem(), ActionOnItemUsePower.TriggerType.STOP, ActionOnItemUsePower.PriorityPhase.ALL);
        }
    }
}
