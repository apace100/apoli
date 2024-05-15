package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.Map;

public record SyncPowersInBulkS2CPacket(int entityId, Map<Identifier, NbtElement> powerAndData) implements FabricPacket {

    public static final PacketType<SyncPowersInBulkS2CPacket> TYPE = PacketType.create(
        Apoli.identifier("s2c/sync_powers_in_bulk"), SyncPowersInBulkS2CPacket::read
    );

    private static SyncPowersInBulkS2CPacket read(PacketByteBuf buf) {
        return new SyncPowersInBulkS2CPacket(buf.readVarInt(), buf.readMap(PacketByteBuf::readIdentifier, PacketByteBuf::readNbt));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeMap(powerAndData, PacketByteBuf::writeIdentifier, PacketByteBuf::writeNbt);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
