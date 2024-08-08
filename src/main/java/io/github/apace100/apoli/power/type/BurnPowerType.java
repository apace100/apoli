package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

public class BurnPowerType extends PowerType {

    private final int interval;
    private final int burnDuration;

    public BurnPowerType(Power power, LivingEntity entity, int interval, int burnDuration) {
        super(power, entity);
        this.interval = interval;
        this.burnDuration = burnDuration;
        this.setTicking();
    }

    public void tick() {

        if (entity.age % interval == 0) {
            entity.setOnFireFor(burnDuration);
        }

    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("burn"),
            new SerializableData()
                .add("interval", SerializableDataTypes.POSITIVE_INT)
                .add("burn_duration", SerializableDataTypes.POSITIVE_INT),
            data -> (power, entity) -> new BurnPowerType(power, entity,
                data.getInt("interval"),
                data.getInt("burn_duration")
            )
        ).allowCondition();
    }

}
