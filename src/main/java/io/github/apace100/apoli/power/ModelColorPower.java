package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

public class ModelColorPower extends Power {

    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private final boolean isTranslucent;

    public ModelColorPower(PowerType type, LivingEntity entity, float red, float green, float blue, float alpha) {
        super(type, entity);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.isTranslucent = alpha < 1.0F;
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
        return isTranslucent;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("model_color"),
            new SerializableData()
                .add("red", SerializableDataTypes.FLOAT, 1.0F)
                .add("green", SerializableDataTypes.FLOAT, 1.0F)
                .add("blue", SerializableDataTypes.FLOAT, 1.0F)
                .add("alpha", SerializableDataTypes.FLOAT, 1.0F),
            data ->
                (type, player) ->
                    new ModelColorPower(type, player, data.getFloat("red"), data.getFloat("green"), data.getFloat("blue"), data.getFloat("alpha")))
            .allowCondition();
    }
}
