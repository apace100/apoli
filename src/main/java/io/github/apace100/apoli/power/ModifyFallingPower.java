package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

public class ModifyFallingPower extends Power {

    public final double velocity;
    public final boolean takeFallDamage;
    public final float damagingFallDistance;

    public ModifyFallingPower(PowerType<?> type, LivingEntity entity, double velocity, boolean takeFallDamage, float damagingFallDistance) {
        super(type, entity);
        this.velocity = velocity;
        this.takeFallDamage = takeFallDamage;
        this.damagingFallDistance = damagingFallDistance;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_falling"),
            new SerializableData()
                .add("velocity", SerializableDataTypes.DOUBLE)
                .add("take_fall_damage", SerializableDataTypes.BOOLEAN, true)
                .add("damaging_fall_distance", SerializableDataTypes.FLOAT, 3.0F),
            data ->
                (type, player) -> new ModifyFallingPower(type, player,
                    data.getDouble("velocity"),
                    data.getBoolean("take_fall_damage"),
                    data.getFloat("damaging_fall_distance")
            ))
            .allowCondition();
    }
}
