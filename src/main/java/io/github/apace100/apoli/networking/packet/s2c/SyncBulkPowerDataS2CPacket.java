package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record SyncBulkPowerDataS2CPacket(int entityId, Map<Identifier, NbtElement> powerAndData) implements CustomPayload {

    public static final Id<SyncBulkPowerDataS2CPacket> PACKET_ID = new Id<>(Apoli.identifier("s2c/sync_bulk_power_data"));
    public static final PacketCodec<PacketByteBuf, SyncBulkPowerDataS2CPacket> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT, SyncBulkPowerDataS2CPacket::entityId,
        PacketCodecs.map(HashMap::new, Identifier.PACKET_CODEC, PacketCodecs.NBT_ELEMENT), SyncBulkPowerDataS2CPacket::powerAndData,
        SyncBulkPowerDataS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
