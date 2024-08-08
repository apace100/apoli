package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

public class NightVisionPowerType extends PowerType {

    private final float strength;

    public NightVisionPowerType(Power power, LivingEntity entity, float strength) {
        super(power, entity);
        this.strength = strength;
    }

    public float getStrength() {
        return strength;
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("night_vision"),
            new SerializableData()
                .add("strength", SerializableDataTypes.FLOAT, 1.0F),
            data -> (power, entity) -> new NightVisionPowerType(power, entity,
                data.getFloat("strength")
            )
        ).allowCondition();
    }

}
