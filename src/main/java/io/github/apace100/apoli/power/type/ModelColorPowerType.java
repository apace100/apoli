package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

public class ModelColorPowerType extends PowerType {

    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;

    public ModelColorPowerType(Power power, LivingEntity entity, float red, float green, float blue, float alpha) {
        super(power, entity);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
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

    public static PowerTypeFactory<ModelColorPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("model_color"),
            new SerializableData()
                .add("red", SerializableDataTypes.FLOAT, 1.0F)
                .add("green", SerializableDataTypes.FLOAT, 1.0F)
                .add("blue", SerializableDataTypes.FLOAT, 1.0F)
                .add("alpha", SerializableDataTypes.FLOAT, 1.0F),
            data -> (power, entity) -> new ModelColorPowerType(power, entity,
                data.getFloat("red"),
                data.getFloat("green"),
                data.getFloat("blue"),
                data.getFloat("alpha")
            )
        ).allowCondition();
    }

}
