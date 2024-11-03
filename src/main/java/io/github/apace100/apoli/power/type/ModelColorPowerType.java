package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ModelColorPowerType extends PowerType {

    public static final TypedDataObjectFactory<ModelColorPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("red", ApoliDataTypes.NORMALIZED_FLOAT, 1.0F)
            .add("green", ApoliDataTypes.NORMALIZED_FLOAT, 1.0F)
            .add("blue", ApoliDataTypes.NORMALIZED_FLOAT, 1.0F)
            .add("alpha", ApoliDataTypes.NORMALIZED_FLOAT, 1.0F),
        (data, condition) -> new ModelColorPowerType(
            data.get("red"),
            data.get("green"),
            data.get("blue"),
            data.get("alpha"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("red", powerType.red)
            .set("green", powerType.green)
            .set("blue", powerType.blue)
            .set("alpha", powerType.alpha)
    );

    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;

    public ModelColorPowerType(float red, float green, float blue, float alpha, Optional<EntityCondition> condition) {
        super(condition);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODEL_COLOR;
    }

    public float getRed() {
        return red;
    }

    public float getGreen() {
        return green;
    }

    public float getBlue() {
        return blue;
    }

    public float getAlpha() {
        return alpha;
    }

    public boolean isTranslucent() {
        return alpha < 1.0F;
    }

}
