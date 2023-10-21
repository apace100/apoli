package io.github.apace100.apoli.networking.packet.c2s;

import io.github.apace100.apoli.Apoli;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

public record PlayerLandedC2SPacket() implements FabricPacket {

    public static final PacketType<PlayerLandedC2SPacket> TYPE = PacketType.create(
        Apoli.identifier("c2s/player_landed"), PlayerLandedC2SPacket::read
    );

    private static PlayerLandedC2SPacket read(PacketByteBuf buffer) {
        return new PlayerLandedC2SPacket();
    }

    @Override
    public void write(PacketByteBuf buf) {

    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
