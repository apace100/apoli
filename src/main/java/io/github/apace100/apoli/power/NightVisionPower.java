package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

public class NightVisionPower extends Power {

    private final float strength;

    public NightVisionPower(PowerType<?> type, LivingEntity entity) {
        this(type, entity, 1.0F);
    }

    public NightVisionPower(PowerType<?> type, LivingEntity entity, float strength) {
        super(type, entity);
        this.strength = strength;
    }

    public float getStrength() {
        return strength;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("night_vision"),
            new SerializableData()
                .add("strength", SerializableDataTypes.FLOAT, 1.0F),
            data -> (type, player) -> new NightVisionPower(
                type,
                player,
                data.getFloat("strength")
            )
        ).allowCondition();
    }

}
