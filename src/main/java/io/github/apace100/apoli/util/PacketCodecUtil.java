package io.github.apace100.apoli.util;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.Set;

public final class PacketCodecUtil {

	public static <B extends ByteBuf, E> PacketCodec.ResultFunction<B, E, Set<E>> toHashSet() {
		return codec -> PacketCodecs.collection(ObjectOpenHashSet::new, codec);
	}

}
