package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.Optional;

public record SyncAttackerS2CPacket(int targetId, Optional<Integer> attackerId) implements CustomPayload {

    public static final Id<SyncAttackerS2CPacket> PACKET_ID = new Id<>(Apoli.identifier("s2c/sync_attacker"));
    public static final PacketCodec<RegistryByteBuf, SyncAttackerS2CPacket> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT, SyncAttackerS2CPacket::targetId,
        PacketCodecs.optional(PacketCodecs.VAR_INT), SyncAttackerS2CPacket::attackerId,
        SyncAttackerS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
