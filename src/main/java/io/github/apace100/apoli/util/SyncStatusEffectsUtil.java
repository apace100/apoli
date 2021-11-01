package io.github.apace100.apoli.util;

import io.github.apace100.apoli.networking.ModPackets;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;

public class SyncStatusEffectsUtil {
    public static void sendStatusEffectUpdatePacket(LivingEntity living) {
        if (living.world.isClient()) return;
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(living.getId());
        buf.writeInt(living.getActiveStatusEffects().size());
        living.getActiveStatusEffects().forEach((effect, instance) -> {
            buf.writeInt(Registry.STATUS_EFFECT.getRawId(effect));
            buf.writeNbt(instance.writeNbt(new NbtCompound()));
        });
        for (ServerPlayerEntity player : PlayerLookup.tracking(living)) {
            ServerPlayNetworking.send(player, ModPackets.SYNC_STATUS_EFFECT, buf);
        }
    }
}