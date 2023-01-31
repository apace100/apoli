package io.github.apace100.apoli.power.factory;

import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface Factory {

    Identifier getSerializerId();

}
