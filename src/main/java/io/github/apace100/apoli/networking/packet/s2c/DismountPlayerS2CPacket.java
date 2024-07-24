package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record DismountPlayerS2CPacket(int id) implements CustomPayload {

    public static final Id<DismountPlayerS2CPacket> PACKET_ID = new Id<>(Apoli.identifier("s2c/dismount_player"));
    public static final PacketCodec<PacketByteBuf, DismountPlayerS2CPacket> PACKET_CODEC = PacketCodecs.VAR_INT.xmap(DismountPlayerS2CPacket::new, DismountPlayerS2CPacket::id).cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
