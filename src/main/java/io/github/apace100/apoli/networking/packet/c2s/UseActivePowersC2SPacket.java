package io.github.apace100.apoli.networking.packet.c2s;

import io.github.apace100.apoli.Apoli;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record UseActivePowersC2SPacket(List<Identifier> powerTypeIds) implements CustomPayload {

    public static final Id<UseActivePowersC2SPacket> PACKET_ID = new Id<>(Apoli.identifier("c2s/use_active_powers"));
    public static final PacketCodec<RegistryByteBuf, UseActivePowersC2SPacket> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.collection(ArrayList::new, Identifier.PACKET_CODEC), UseActivePowersC2SPacket::powerTypeIds,
        UseActivePowersC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
