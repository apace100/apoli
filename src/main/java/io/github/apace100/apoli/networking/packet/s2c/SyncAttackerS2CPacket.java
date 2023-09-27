package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

import java.util.OptionalInt;

public record SyncAttackerS2CPacket(int targetId, OptionalInt attackerId) implements FabricPacket {

    public static final PacketType<SyncAttackerS2CPacket> TYPE = PacketType.create(
        Apoli.identifier("s2c/set_attacker"), SyncAttackerS2CPacket::read
    );

    private static SyncAttackerS2CPacket read(PacketByteBuf buffer) {
        OptionalInt attackerId = buffer.readBoolean() ? OptionalInt.of(buffer.readVarInt()) : OptionalInt.empty();
        return new SyncAttackerS2CPacket(buffer.readVarInt(), attackerId);
    }

    @Override
    public void write(PacketByteBuf buffer) {

        buffer.writeBoolean(attackerId.isPresent());
        buffer.writeVarInt(targetId);

        attackerId.ifPresent(buffer::writeVarInt);

    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
