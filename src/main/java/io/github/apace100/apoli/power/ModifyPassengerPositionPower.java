package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

import java.util.function.Predicate;

public class ModifyPassengerPositionPower extends Power {

    private final Predicate<Pair<Entity, Entity>> biEntityCondition;
    private final Vec3d positionOffset;

    public ModifyPassengerPositionPower(PowerType<?> type, LivingEntity entity, Predicate<Pair<Entity, Entity>> biEntityCondition, Vec3d positionOffset) {
        super(type, entity);
        this.biEntityCondition = biEntityCondition;
        this.positionOffset = positionOffset;
    }

    public boolean doesApply(Entity passenger) {
        return biEntityCondition == null || biEntityCondition.test(new Pair<>(passenger, entity));
    }

    public Vec3d getPositionOffset() {
        return positionOffset;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("modify_passenger_position"),
            new SerializableData()
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("offset", SerializableDataTypes.VECTOR),
            data -> (powerType, livingEntity) -> new ModifyPassengerPositionPower(
                powerType,
                livingEntity,
                data.get("bientity_condition"),
                data.get("offset")
            )
        ).allowCondition();
    }

}
