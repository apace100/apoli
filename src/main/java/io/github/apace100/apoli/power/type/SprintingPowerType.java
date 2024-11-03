package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SprintingPowerType extends PowerType {

    public static final TypedDataObjectFactory<SprintingPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("requires_input", SerializableDataTypes.BOOLEAN, false),
        (data, condition) -> new SprintingPowerType(
            data.get("requires_input"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("requires_input", powerType.shouldRequireInput())
    );

    private final boolean requiresInput;

    public SprintingPowerType(boolean requiresInput, Optional<EntityCondition> condition) {
        super(condition);
        this.requiresInput = requiresInput;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.SPRINTING;
    }

    public boolean shouldRequireInput() {
        return requiresInput;
    }

}
