package io.github.apace100.apoli.util;

import com.mojang.serialization.MapCodec;
import io.github.apace100.calio.data.CompoundSerializableDataType;
import net.minecraft.util.Identifier;

public interface TypeConfiguration<T> {

	Identifier id();

	CompoundSerializableDataType<T> dataType();

	default CompoundSerializableDataType<T> dataType(boolean root) {
		return dataType().setRoot(root);
	}

	default MapCodec<T> mapCodec() {
		return dataType().mapCodec();
	}

	default MapCodec<T> mapCodec(boolean root) {
		return dataType(root).mapCodec();
	}

}
