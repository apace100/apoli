package io.github.apace100.apoli.power.type.meta;

import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.power.type.PowerTypes;
import org.jetbrains.annotations.NotNull;

public final class MultiplePowerType extends PowerType {

	@Override
	public @NotNull PowerConfiguration<?> getConfig() {
		return PowerTypes.MULTIPLE;
	}

}
