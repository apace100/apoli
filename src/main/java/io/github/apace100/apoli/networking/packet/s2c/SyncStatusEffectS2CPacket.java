package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.util.SyncStatusEffectsUtil;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public record SyncStatusEffectS2CPacket(int targetId, NbtCompound statusEffectData, SyncStatusEffectsUtil.UpdateType updateType) implements FabricPacket {

    public static final PacketType<SyncStatusEffectS2CPacket> TYPE = PacketType.create(
        Apoli.identifier("s2c/sync_status_effect"), SyncStatusEffectS2CPacket::read
    );

    private static SyncStatusEffectS2CPacket read(PacketByteBuf buffer) {
        SyncStatusEffectsUtil.UpdateType updateType = SyncStatusEffectsUtil.UpdateType.values()[buffer.readVarInt()];
        return new SyncStatusEffectS2CPacket(buffer.readVarInt(), buffer.readNbt(), updateType);
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeVarInt(updateType.ordinal());
        buffer.writeVarInt(targetId);
        buffer.writeNbt(statusEffectData);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
