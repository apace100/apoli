package io.github.apace100.apoli.networking.packet;

import io.github.apace100.apoli.Apoli;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

public record VersionHandshakePacket(int[] semver) implements FabricPacket {

    public static final PacketType<VersionHandshakePacket> TYPE = PacketType.create(
        Apoli.identifier("handshake/version"), VersionHandshakePacket::read
    );

    public static VersionHandshakePacket read(PacketByteBuf buffer) {
        return new VersionHandshakePacket(buffer.readIntArray());
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeIntArray(semver);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
