package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

import java.util.function.Function;

public class NightVisionPower extends Power {

    private final Function<LivingEntity, Float> strengthFunction;

    public NightVisionPower(PowerType<?> type, LivingEntity entity) {
        this(type, entity, 1.0F);
    }

    public NightVisionPower(PowerType<?> type, LivingEntity entity, float strength) {
        this(type, entity, pe -> strength);
    }

    public NightVisionPower(PowerType<?> type, LivingEntity entity, Function<LivingEntity, Float> strengthFunction) {
        super(type, entity);
        this.strengthFunction = strengthFunction;
    }

    public float getStrength() {
        return strengthFunction.apply(this.entity);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("night_vision"),
            new SerializableData()
                .add("strength", SerializableDataTypes.FLOAT, 1.0F),
            data ->
                (type, player) ->
                    new NightVisionPower(type, player, data.getFloat("strength")))
            .allowCondition();
    }
}
