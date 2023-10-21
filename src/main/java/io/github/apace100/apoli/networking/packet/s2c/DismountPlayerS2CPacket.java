package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

public record DismountPlayerS2CPacket(int id) implements FabricPacket {

    public static final PacketType<DismountPlayerS2CPacket> TYPE = PacketType.create(
        Apoli.identifier("s2c/dismount_player"), DismountPlayerS2CPacket::read
    );

    private static DismountPlayerS2CPacket read(PacketByteBuf buffer) {
        return new DismountPlayerS2CPacket(buffer.readVarInt());
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeVarInt(id);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
