package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.power.PowerConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ShakingPowerType extends PowerType {

    public ShakingPowerType(Optional<EntityCondition> condition) {
        super(condition);
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.SHAKING;
    }

}
