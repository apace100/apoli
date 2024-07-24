package io.github.apace100.apoli.networking.packet;

import io.github.apace100.apoli.Apoli;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record VersionHandshakePacket(int[] semver) implements CustomPayload {

    public static final Id<VersionHandshakePacket> PACKET_ID = new Id<>(Apoli.identifier("handshake/version"));
    public static final PacketCodec<PacketByteBuf, VersionHandshakePacket> PACKET_CODEC = PacketCodec.of(VersionHandshakePacket::write, VersionHandshakePacket::read);

    public static VersionHandshakePacket read(PacketByteBuf buf) {
        return new VersionHandshakePacket(buf.readIntArray());
    }

    public void write(PacketByteBuf buf) {
        buf.writeIntArray(semver);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
