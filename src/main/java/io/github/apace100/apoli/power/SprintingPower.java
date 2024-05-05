package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.mixin.LivingEntityAccessor;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

public class SprintingPower extends Power {

    private final boolean requiresForwardMovement;

    public SprintingPower(PowerType<?> type, LivingEntity entity, boolean requiresForwardMovement) {
        super(type, entity);
        this.requiresForwardMovement = requiresForwardMovement;
    }

    public boolean shouldRequireForwardMovement() {
        return requiresForwardMovement;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
                Apoli.identifier("sprinting"),
                new SerializableData()
                        .addFunctionedDefault("requires_forward_movement", SerializableDataTypes.BOOLEAN, data -> data.isPresent("requires_input") && data.getBoolean("requires_input"))
                        // Backwards compat for those using the Apugli power.
                        .add("requires_input", SerializableDataTypes.BOOLEAN, false),
                data -> (powerType, entity) -> new SprintingPower(powerType, entity, data.getBoolean("requires_forward_movement"))
        ).allowCondition();
    }
}
