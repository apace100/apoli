package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.modifier.Modifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ModifyHealingPowerType extends ValueModifyingPowerType {

    public ModifyHealingPowerType(List<Modifier> modifiers, Optional<EntityCondition> condition) {
        super(modifiers, condition);
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_HEALING;
    }

}
