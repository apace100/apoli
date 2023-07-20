package io.github.apace100.apoli.util;

import io.github.apace100.apoli.networking.ModPackets;
import io.github.apace100.calio.SerializationHelper;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class SyncStatusEffectsUtil {

    public static void sendStatusEffectUpdatePacket(LivingEntity living, UpdateType type, StatusEffectInstance instance) {
        if (living.getWorld().isClient()) return;
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(living.getId());
        buf.writeByte(type.ordinal());
        if(type != UpdateType.CLEAR) {
            SerializationHelper.writeStatusEffect(buf, instance);
        }
        for (ServerPlayerEntity player : PlayerLookup.tracking(living)) {
            ServerPlayNetworking.send(player, ModPackets.SYNC_STATUS_EFFECT, buf);
        }
    }

    public enum UpdateType {
        CLEAR, APPLY, UPGRADE, REMOVE
    }
}