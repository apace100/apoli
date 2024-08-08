package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ExhaustOverTimePowerType extends PowerType {

    private final int exhaustInterval;
    private final float exhaustion;

    public ExhaustOverTimePowerType(Power power, LivingEntity entity, int exhaustInterval, float exhaustion) {
        super(power, entity);
        this.exhaustInterval = exhaustInterval;
        this.exhaustion = exhaustion;
        this.setTicking();
    }

    public void tick() {

        if (entity instanceof PlayerEntity playerEntity && entity.age % exhaustInterval == 0) {
            playerEntity.addExhaustion(exhaustion);
        }

    }

    public static PowerTypeFactory<ExhaustOverTimePowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("exhaust"),
            new SerializableData()
                .add("interval", SerializableDataTypes.POSITIVE_INT, 20)
                .add("exhaustion", SerializableDataTypes.FLOAT),
            data -> (power, entity) -> new ExhaustOverTimePowerType(power, entity,
                data.getInt("interval"),
                data.getFloat("exhaustion")
            )
        ).allowCondition();
    }
}
