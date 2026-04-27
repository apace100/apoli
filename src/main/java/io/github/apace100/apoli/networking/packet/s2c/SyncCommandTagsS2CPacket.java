package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.List;
import java.util.Set;

public record SyncCommandTagsS2CPacket(Set<String> tags, int entityId) implements CustomPayload {

	private static final PacketCodec<ByteBuf, List<String>> STRINGS_CODEC = PacketCodecs.STRING.collect(PacketCodecs.toList());
	private static final PacketCodec<ByteBuf, Set<String>> STRING_SET_CODEC = STRINGS_CODEC.xmap(Set::copyOf, List::copyOf);

	public static final Id<SyncCommandTagsS2CPacket> ID = new Id<>(Apoli.identifier("s2c/sync_command_tags"));
	public static final PacketCodec<ByteBuf, SyncCommandTagsS2CPacket> CODEC = PacketCodec.tuple(
		STRING_SET_CODEC, SyncCommandTagsS2CPacket::tags,
		PacketCodecs.INTEGER, SyncCommandTagsS2CPacket::entityId,
		SyncCommandTagsS2CPacket::new
	);

	public SyncCommandTagsS2CPacket(Entity entity) {
		this(entity.getCommandTags(), entity.getId());
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

}
