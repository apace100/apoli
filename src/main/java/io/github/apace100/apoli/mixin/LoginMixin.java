package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.EndRespawningEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyPlayerSpawnPower;
import io.github.apace100.apoli.power.Power;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = PlayerManager.class, priority = 800)
public abstract class LoginMixin {

	@WrapOperation(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSpawnPoint(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/util/math/BlockPos;FZZ)V"))
	private void apoli$preventEndExitSpawnPointSetting(ServerPlayerEntity instance, RegistryKey<World> dimension, @Nullable BlockPos pos, float angle, boolean forced, boolean sendMessage, Operation<Void> original, ServerPlayerEntity mPlayer, boolean mAlive) {
		if (((EndRespawningEntity) mPlayer).apoli$hasRealRespawnPoint()) {
			original.call(instance, dimension, pos, angle, forced, sendMessage);
		}
	}

	@Inject(method = "remove", at = @At("HEAD"))
	private void apoli$invokeOnRemovedPowerCallbackOnRemoved(ServerPlayerEntity player, CallbackInfo ci) {
		PowerHolderComponent.KEY.get(player).getPowers().forEach(power -> {
			power.onRemoved();
			power.onRemoved(false);
		});
	}

	@WrapOperation(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;findRespawnPosition(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;FZZ)Ljava/util/Optional;"))
	private Optional<Vec3d> apoli$retryObstructedSpawnpointIfFailed(ServerWorld world, BlockPos pos, float angle, boolean forced, boolean alive, Operation<Optional<Vec3d>> original, ServerPlayerEntity mPlayer) {

		Optional<Vec3d> originalPosition = original.call(world, pos, angle, forced, alive);
		if (originalPosition.isEmpty() && PowerHolderComponent.hasPower(mPlayer, ModifyPlayerSpawnPower.class)) {
			return Optional.ofNullable(Dismounting.findRespawnPos(EntityType.PLAYER, world, pos, forced));
		}

		return originalPosition;

	}

	@Inject(method = "respawnPlayer", at = @At("HEAD"))
	private void apoli$invokeOnRemovedPowerCallbackOnRespawn(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir) {
		PowerHolderComponent.KEY.get(player).getPowers().forEach(power -> {
			power.onRemoved();
			power.onRemoved(false);
		});
	}

	@Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;onSpawn()V"))
	private void apoli$invokeOnRespawnPowerCallback(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir, @Local(ordinal = 1) ServerPlayerEntity newPlayer) {
		if (!alive) {
			PowerHolderComponent.KEY.get(newPlayer).getPowers().forEach(Power::onRespawn);
		}
	}

}
