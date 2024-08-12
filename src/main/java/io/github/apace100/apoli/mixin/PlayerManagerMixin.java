package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.EndRespawningEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.PowerType;
import net.minecraft.entity.Entity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerManager.class, priority = 800)
public abstract class PlayerManagerMixin {

	@WrapWithCondition(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSpawnPointFrom(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
	private boolean apoli$preventEndExitSpawnpointResetting(ServerPlayerEntity newPlayer, ServerPlayerEntity oldPlayer) {
		return ((EndRespawningEntity) oldPlayer).apoli$hasRealRespawnPoint();
	}

	@Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;onSpawn()V"))
	private void apoli$invokeOnRespawnPowerCallback(ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir, @Local(ordinal = 1) ServerPlayerEntity newPlayer) {
		if (!alive) {
			PowerHolderComponent.KEY.get(newPlayer).getPowerTypes().forEach(PowerType::onRespawn);
		}
	}

}
