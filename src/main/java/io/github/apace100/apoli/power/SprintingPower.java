package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.mixin.LivingEntityAccessor;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

public class SprintingPower extends Power {

    private final boolean requiresInput;

    public SprintingPower(PowerType<?> type, LivingEntity entity, boolean requiresInput) {
        super(type, entity);
        this.requiresInput = requiresInput;
    }

    public boolean shouldRequireInput() {
        return requiresInput;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
                Apoli.identifier("sprinting"),
                new SerializableData()
                        .add("requires_input", SerializableDataTypes.BOOLEAN, false),
                data -> (powerType, entity) -> new SprintingPower(powerType, entity, data.getBoolean("requires_input"))
        ).allowCondition();
    }
}
