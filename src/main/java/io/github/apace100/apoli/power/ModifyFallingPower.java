package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

public class ModifyFallingPower extends Power {

    public final double velocity;
    public final boolean takeFallDamage;

    public ModifyFallingPower(PowerType<?> type, LivingEntity entity, double velocity, boolean takeFallDamage) {
        super(type, entity);
        this.velocity = velocity;
        this.takeFallDamage = takeFallDamage;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_falling"),
            new SerializableData()
                .add("velocity", SerializableDataTypes.DOUBLE)
                .add("take_fall_damage", SerializableDataTypes.BOOLEAN, true),
            data ->
                (type, player) -> new ModifyFallingPower(type, player, data.getDouble("velocity"), data.getBoolean("take_fall_damage")))
            .allowCondition();
    }
}
