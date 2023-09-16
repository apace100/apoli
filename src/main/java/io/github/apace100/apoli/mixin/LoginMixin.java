package io.github.apace100.apoli.mixin;

import com.google.common.collect.ImmutableList;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import io.github.apace100.apoli.access.EndRespawningEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.ModPackets;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("rawtypes")
@Mixin(value = PlayerManager.class, priority = 800)
public abstract class LoginMixin {

	@Shadow public abstract List<ServerPlayerEntity> getPlayerList();

	@Inject(at = @At("TAIL"), method = "Lnet/minecraft/server/PlayerManager;onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V")
	private void syncPowerTypes(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
		PacketByteBuf powerListData = new PacketByteBuf(Unpooled.buffer());
		powerListData.writeInt(PowerTypeRegistry.size());
		PowerTypeRegistry.entries().forEach((entry) -> {
			PowerType<?> type = entry.getValue();
			PowerFactory.Instance factory = type.getFactory();
			if(factory != null) {
				powerListData.writeIdentifier(entry.getKey());
				factory.write(powerListData);
				if(type instanceof MultiplePowerType) {
					powerListData.writeBoolean(true);
					ImmutableList<Identifier> subPowers = ((MultiplePowerType<?>)type).getSubPowers();
					powerListData.writeVarInt(subPowers.size());
					subPowers.forEach(powerListData::writeIdentifier);
				} else {
					powerListData.writeBoolean(false);
				}
				powerListData.writeString(type.getOrCreateNameTranslationKey());
				powerListData.writeString(type.getOrCreateDescriptionTranslationKey());
				powerListData.writeBoolean(type.isHidden());
			}
		});

		ServerPlayNetworking.send(player, ModPackets.POWER_LIST, powerListData);

		List<ServerPlayerEntity> playerList = getPlayerList();
		playerList.forEach(spe -> {
			PowerHolderComponent.KEY.syncWith(spe, (ComponentProvider) player);
			PowerHolderComponent.KEY.syncWith(player, (ComponentProvider) spe);
		});
		PowerHolderComponent.sync(player);
	}

	@Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSpawnPoint(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/util/math/BlockPos;FZZ)V"))
	private void preventEndExitSpawnPointSetting(ServerPlayerEntity serverPlayerEntity, RegistryKey<World> dimension, BlockPos pos, float angle, boolean spawnPointSet, boolean bl, ServerPlayerEntity playerEntity, boolean alive) {
		EndRespawningEntity ere = (EndRespawningEntity)playerEntity;
		// Prevent setting the spawn point if the player has a "fake" respawn point
		if(ere.apoli$hasRealRespawnPoint()) {
			serverPlayerEntity.setSpawnPoint(dimension, pos, angle, spawnPointSet, bl);
		}
	}

	@Inject(method = "remove", at = @At("HEAD"))
	private void invokeOnRemovedCallback(ServerPlayerEntity player, CallbackInfo ci) {
		PowerHolderComponent component = PowerHolderComponent.KEY.get(player);
		component.getPowers().forEach(Power::onRemoved);
	}

	@Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;findRespawnPosition(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;FZZ)Ljava/util/Optional;"))
	private Optional<Vec3d> retryObstructedSpawnpointIfFailed(ServerWorld world, BlockPos pos, float f, boolean bl, boolean bl2, ServerPlayerEntity player, boolean alive) {
		Optional<Vec3d> original = PlayerEntity.findRespawnPosition(world, pos, f, bl, bl2);
		if(!original.isPresent()) {
			if(PowerHolderComponent.hasPower(player, ModifyPlayerSpawnPower.class)) {
				return Optional.ofNullable(Dismounting.findRespawnPos(EntityType.PLAYER, world, pos, bl));
			}
		}
		return original;
	}

	@Inject(method = "respawnPlayer", at = @At("HEAD"))
	private void invokePowerRemovedCallback(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir) {
		List<Power> powers = PowerHolderComponent.KEY.get(player).getPowers();
		powers.forEach(Power::onRemoved);
	}

	@Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;onSpawn()V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void invokePowerRespawnCallback(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir, BlockPos blockPos, float f, boolean bl, ServerWorld serverWorld, Optional optional2, ServerWorld serverWorld2, ServerPlayerEntity serverPlayerEntity, boolean b) {
		if(!alive) {
			List<Power> powers = PowerHolderComponent.KEY.get(serverPlayerEntity).getPowers();
			powers.forEach(Power::onRespawn);
		}
	}
}
