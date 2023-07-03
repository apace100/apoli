package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ExhaustOverTimePower extends Power {

    private final int exhaustInterval;
    private final float exhaustion;

    public ExhaustOverTimePower(PowerType<?> type, LivingEntity entity, int exhaustInterval, float exhaustion) {
        super(type, entity);
        if(exhaustInterval <= 0) exhaustInterval = 1;
        this.exhaustInterval = exhaustInterval;
        this.exhaustion = exhaustion;
        this.setTicking();
    }

    public void tick() {
        if(entity instanceof PlayerEntity && entity.age % exhaustInterval == 0) {
            ((PlayerEntity)entity).addExhaustion(exhaustion);
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("exhaust"),
            new SerializableData()
                .add("interval", SerializableDataTypes.INT, 20)
                .add("exhaustion", SerializableDataTypes.FLOAT),
            data ->
                (type, player) -> new ExhaustOverTimePower(type, player, data.getInt("interval"), data.getFloat("exhaustion")))
            .allowCondition();
    }
}
