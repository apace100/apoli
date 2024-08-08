package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

public class SprintingPowerType extends PowerType {

    private final boolean requiresInput;

    public SprintingPowerType(Power power, LivingEntity entity, boolean requiresInput) {
        super(power, entity);
        this.requiresInput = requiresInput;
    }

    public boolean shouldRequireInput() {
        return requiresInput;
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("sprinting"),
            new SerializableData()
                .add("requires_input", SerializableDataTypes.BOOLEAN, false),
            data -> (power, entity) -> new SprintingPowerType(power, entity,
                data.get("requires_input")
            )
        ).allowCondition();
    }

}
