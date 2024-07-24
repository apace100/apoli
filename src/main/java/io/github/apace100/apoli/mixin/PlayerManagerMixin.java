package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.EndRespawningEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyPlayerSpawnPower;
import io.github.apace100.apoli.power.Power;
import net.minecraft.entity.Entity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerManager.class, priority = 800)
public abstract class PlayerManagerMixin {

	@WrapWithCondition(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSpawnPointFrom(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
	private boolean apoli$preventEndExitSpawnpointResetting(ServerPlayerEntity newPlayer, ServerPlayerEntity oldPlayer) {
		return !(oldPlayer instanceof EndRespawningEntity endRespawningEntity)
			|| endRespawningEntity.apoli$hasRealRespawnPoint();
	}

	@Inject(method = "remove", at = @At("HEAD"))
	private void apoli$invokeOnRemovedPowerCallbackOnRemoved(ServerPlayerEntity player, CallbackInfo ci) {
		PowerHolderComponent.KEY.get(player).getPowers().forEach(power -> {
			power.onRemoved();
			power.onRemoved(false);
		});
	}

	@WrapOperation(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getRespawnTarget(ZLnet/minecraft/world/TeleportTarget$PostDimensionTransition;)Lnet/minecraft/world/TeleportTarget;"))
	private TeleportTarget apoli$retryObstructedSpawnpointIfFailed(ServerPlayerEntity oldPlayer, boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, Operation<TeleportTarget> original) {

		TeleportTarget originalSpawnTarget = original.call(oldPlayer, alive, postDimensionTransition);
		if (!originalSpawnTarget.missingRespawnBlock() || !PowerHolderComponent.hasPower(oldPlayer, ModifyPlayerSpawnPower.class)) {
			return originalSpawnTarget;
		}

		float spawnAngle = oldPlayer.getSpawnAngle();

		ServerWorld world = oldPlayer.getServerWorld();
		BlockPos spawnPointPos = oldPlayer.getSpawnPointPosition();

		return ServerPlayerEntityAccessor.callFindRespawnPosition(world, spawnPointPos, spawnAngle, true, alive)
			.map(respawnPos -> new TeleportTarget(world, respawnPos.pos(), Vec3d.ZERO, respawnPos.yaw(), 0.0F, postDimensionTransition))
			.orElse(originalSpawnTarget);

	}

	@Inject(method = "respawnPlayer", at = @At("HEAD"))
	private void apoli$invokeOnRemovedPowerCallbackOnRespawn(ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir) {
		PowerHolderComponent.KEY.get(player).getPowers().forEach(power -> {
			power.onRemoved();
			power.onRemoved(false);
		});
	}

	@Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;onSpawn()V"))
	private void apoli$invokeOnRespawnPowerCallback(ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir, @Local(ordinal = 1) ServerPlayerEntity newPlayer) {
		if (!alive) {
			PowerHolderComponent.KEY.get(newPlayer).getPowers().forEach(Power::onRespawn);
		}
	}

}
