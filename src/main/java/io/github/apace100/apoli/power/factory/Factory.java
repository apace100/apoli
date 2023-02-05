package io.github.apace100.apoli.power.factory;

import io.github.apace100.calio.data.SerializableData;
import net.minecraft.util.Identifier;

public interface Factory {

    Identifier getSerializerId();

    SerializableData getSerializableData();

}
