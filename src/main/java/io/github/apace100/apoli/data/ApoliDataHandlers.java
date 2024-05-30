package io.github.apace100.apoli.data;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.PacketByteBuf;

import java.util.HashSet;
import java.util.Set;

public class ApoliDataHandlers {

    public static final TrackedDataHandler<Set<String>> STRING_SET = TrackedDataHandler.of(
        (buf, strings) -> buf.writeCollection(strings, PacketByteBuf::writeString),
        buf -> buf.readCollection(value -> new HashSet<>(), PacketByteBuf::readString)
    );

    public static void register() {
        TrackedDataHandlerRegistry.register(STRING_SET);
    }

}
