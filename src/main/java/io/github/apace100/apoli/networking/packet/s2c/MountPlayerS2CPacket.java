package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

public record MountPlayerS2CPacket(int actorId, int targetId) implements FabricPacket {

    public static final PacketType<MountPlayerS2CPacket> TYPE = PacketType.create(
        Apoli.identifier("s2c/mount_player"), MountPlayerS2CPacket::read
    );

    private static MountPlayerS2CPacket read(PacketByteBuf buffer) {
        return new MountPlayerS2CPacket(buffer.readVarInt(), buffer.readVarInt());
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeVarInt(actorId);
        buffer.writeVarInt(targetId);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
