package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record SyncPowerS2CPacket(int entityId, Identifier powerTypeId, NbtCompound powerData) implements FabricPacket {

    public static final PacketType<SyncPowerS2CPacket> TYPE = PacketType.create(
        Apoli.identifier("s2c/sync_power"), SyncPowerS2CPacket::read
    );

    private static SyncPowerS2CPacket read(PacketByteBuf buffer) {
        return new SyncPowerS2CPacket(buffer.readVarInt(), buffer.readIdentifier(), buffer.readNbt());
    }

    @Override
    public void write(PacketByteBuf buffer) {

        buffer.writeVarInt(entityId);
        buffer.writeIdentifier(powerTypeId);

        buffer.writeNbt(powerData);

    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
