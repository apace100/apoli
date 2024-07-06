package io.github.apace100.apoli.data;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.codec.PacketCodecs;

import java.util.HashSet;
import java.util.Set;

public class ApoliDataHandlers {

    public static final TrackedDataHandler<Set<String>> STRING_SET = TrackedDataHandler.create(PacketCodecs.collection(HashSet::new, PacketCodecs.string(32767)));

    public static void register() {
        TrackedDataHandlerRegistry.register(STRING_SET);
    }

}
