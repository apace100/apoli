package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.util.PacketCodecUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.Set;

public record SyncCommandTagsS2CPacket(Set<String> tags, int entityId) implements CustomPayload {

	public static final Id<SyncCommandTagsS2CPacket> ID = new Id<>(Apoli.identifier("s2c/sync_command_tags"));
	public static final PacketCodec<ByteBuf, SyncCommandTagsS2CPacket> CODEC = PacketCodec.tuple(
		PacketCodecs.STRING.collect(PacketCodecUtil.toHashSet()), SyncCommandTagsS2CPacket::tags,
		PacketCodecs.INTEGER, SyncCommandTagsS2CPacket::entityId,
		SyncCommandTagsS2CPacket::new
	);

	public SyncCommandTagsS2CPacket(Entity entity) {
		this(new ObjectOpenHashSet<>(entity.getCommandTags()), entity.getId());
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

}
