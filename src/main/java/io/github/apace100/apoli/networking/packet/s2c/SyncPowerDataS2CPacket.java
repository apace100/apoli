package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncPowerDataS2CPacket(int entityId, Identifier powerTypeId, NbtCompound powerData) implements CustomPayload {

    public static final Id<SyncPowerDataS2CPacket> PACKET_ID = new Id<>(Apoli.identifier("s2c/sync_power_data"));
    public static final PacketCodec<RegistryByteBuf, SyncPowerDataS2CPacket> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT, SyncPowerDataS2CPacket::entityId,
        Identifier.PACKET_CODEC, SyncPowerDataS2CPacket::powerTypeId,
        PacketCodecs.NBT_COMPOUND, SyncPowerDataS2CPacket::powerData,
        SyncPowerDataS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
