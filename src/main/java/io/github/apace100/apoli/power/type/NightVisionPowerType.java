package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class NightVisionPowerType extends PowerType {

    public static final TypedDataObjectFactory<NightVisionPowerType> DATA_FACTORY = createConditionedDataFactory(
        new SerializableData()
            .add("strength", SerializableDataTypes.FLOAT, 1.0F),
        (data, condition) -> new NightVisionPowerType(
            data.get("strength"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("strength", powerType.strength)
    );

    private final float strength;

    public NightVisionPowerType(float strength, Optional<EntityCondition> condition) {
        super(condition);
        this.strength = strength;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.NIGHT_VISION;
    }

    public float getStrength() {
        return strength;
    }

}
