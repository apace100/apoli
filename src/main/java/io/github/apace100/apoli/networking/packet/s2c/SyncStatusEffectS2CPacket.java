package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.util.SyncStatusEffectsUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SyncStatusEffectS2CPacket(int targetId, NbtCompound statusEffectData, SyncStatusEffectsUtil.UpdateType updateType) implements CustomPayload {

    public static final Id<SyncStatusEffectS2CPacket> PACKET_ID = new Id<>(Apoli.identifier("s2c/sync_status_effect"));
    public static final PacketCodec<RegistryByteBuf, SyncStatusEffectS2CPacket> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT, SyncStatusEffectS2CPacket::targetId,
        PacketCodecs.UNLIMITED_NBT_COMPOUND, SyncStatusEffectS2CPacket::statusEffectData,
        SyncStatusEffectsUtil.UpdateType.PACKET_CODEC, SyncStatusEffectS2CPacket::updateType,
        SyncStatusEffectS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
