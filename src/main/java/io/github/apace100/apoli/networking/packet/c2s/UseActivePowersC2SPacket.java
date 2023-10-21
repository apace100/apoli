package io.github.apace100.apoli.networking.packet.c2s;

import io.github.apace100.apoli.Apoli;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;

public record UseActivePowersC2SPacket(List<Identifier> powerTypeIds) implements FabricPacket {

    public static final PacketType<UseActivePowersC2SPacket> TYPE = PacketType.create(
        Apoli.identifier("c2s/use_active_powers"), UseActivePowersC2SPacket::read
    );

    private static UseActivePowersC2SPacket read(PacketByteBuf buffer) {

        List<Identifier> powerTypeIds = new LinkedList<>();
        int powerTypeIdsSize = buffer.readVarInt();

        for (int i = 0; i < powerTypeIdsSize; i++) {
            powerTypeIds.add(buffer.readIdentifier());
        }

        return new UseActivePowersC2SPacket(powerTypeIds);

    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeVarInt(powerTypeIds.size());
        powerTypeIds.forEach(buffer::writeIdentifier);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
