package io.github.apace100.apoli.mixin.misc.command_tags_sync;

import io.github.apace100.apoli.networking.packet.s2c.SyncCommandTagsS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public abstract class EntityTrackerEntryMixin {

	@Shadow
	@Final
	private Entity entity;

	@Inject(method = "startTracking", at = @At("TAIL"))
	private void syncCommandTagsAfterTracking(ServerPlayerEntity player, CallbackInfo ci) {

		SyncCommandTagsS2CPacket packet = new SyncCommandTagsS2CPacket(this.entity);

		if (ServerPlayNetworking.canSend(player, packet.getId())) {
			ServerPlayNetworking.send(player, packet);
		}

	}

}
