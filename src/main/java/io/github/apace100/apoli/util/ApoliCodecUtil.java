package io.github.apace100.apoli.util;

import com.mojang.datafixers.util.Either;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.function.Function;

public class ApoliCodecUtil {

    public static <P, S, B extends ByteBuf> PacketCodec<B, P> withAlternativePacketCodec(PacketCodec<B, P> primary, PacketCodec<B, S> secondary, Function<S, P> converter) {
        return PacketCodecs.either(
            primary,
            secondary
        ).xmap(
            fsEither -> fsEither.map(p -> p, converter),
            Either::left
        );
    }

}
