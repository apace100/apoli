package io.github.apace100.apoli.power.type.meta;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.power.type.PowerTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class DummyPowerType extends PowerType {

	public DummyPowerType(Optional<EntityCondition> condition) {
		super(condition);
	}

	@Override
	public @NotNull PowerConfiguration<?> getConfig() {
		return PowerTypes.DUMMY;
	}

}
