package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

public class BurnPower extends Power {

    private final int refreshInterval;
    private final int burnDuration;

    public BurnPower(PowerType<?> type, LivingEntity entity, int refreshInterval, int burnDuration) {
        super(type, entity);
        if(refreshInterval <= 0) refreshInterval = 1;
        this.refreshInterval = refreshInterval;
        this.burnDuration = burnDuration;
        this.setTicking();
    }

    public void tick() {
        if(entity.age % refreshInterval == 0) {
            entity.setOnFireFor(burnDuration);
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("burn"),
            new SerializableData()
                .add("interval", SerializableDataTypes.INT)
                .add("burn_duration", SerializableDataTypes.INT),
            data ->
                (type, player) ->
                    new BurnPower(type, player, data.getInt("interval"), data.getInt("burn_duration")))
            .allowCondition();
    }
}