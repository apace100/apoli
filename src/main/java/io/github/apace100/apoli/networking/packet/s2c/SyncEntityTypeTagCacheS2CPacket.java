package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public record SyncEntityTypeTagCacheS2CPacket(Map<Identifier, Collection<Identifier>> subTags) implements CustomPayload {

	public static final Id<SyncEntityTypeTagCacheS2CPacket> PACKET_ID = new Id<>(Apoli.identifier("s2c/sync_entity_type_tag_cache"));
	public static final PacketCodec<PacketByteBuf, SyncEntityTypeTagCacheS2CPacket> PACKET_CODEC = PacketCodec.of(SyncEntityTypeTagCacheS2CPacket::write, SyncEntityTypeTagCacheS2CPacket::read);

	private static SyncEntityTypeTagCacheS2CPacket read(PacketByteBuf buf) {
		return new SyncEntityTypeTagCacheS2CPacket(buf.readMap(PacketByteBuf::readIdentifier, valBuf -> valBuf.readCollection(ArrayList::new, PacketByteBuf::readIdentifier)));
	}

	private void write(PacketByteBuf buf) {
		buf.writeMap(subTags, PacketByteBuf::writeIdentifier, (valBuf, value) -> valBuf.writeCollection(value, PacketByteBuf::writeIdentifier));
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}

}
