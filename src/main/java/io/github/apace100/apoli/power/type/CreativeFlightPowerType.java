package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.VanillaAbilities;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CreativeFlightPowerType extends PlayerAbilityPowerType {

	public CreativeFlightPowerType(Optional<EntityCondition> condition) {
		super(VanillaAbilities.ALLOW_FLYING, AbilitySource.DEFAULT, condition);
	}

	@Override
	public @NotNull PowerConfiguration<?> getConfig() {
		return PowerTypes.CREATIVE_FLIGHT;
	}

}
