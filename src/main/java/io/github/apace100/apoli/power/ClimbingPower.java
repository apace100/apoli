package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.function.Predicate;

public class ClimbingPower extends Power {

    private final Predicate<Entity> holdingCondition;
    private final boolean allowHolding;

    public ClimbingPower(PowerType<?> type, LivingEntity entity, Predicate<Entity> holdingCondition, boolean allowHolding) {
        super(type, entity);
        this.holdingCondition = holdingCondition != null ? holdingCondition : Entity::isSneaking;
        this.allowHolding = allowHolding;
    }

    public boolean canHold() {
        return allowHolding && holdingCondition.test(entity);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("climbing"),
            new SerializableData()
                .add("hold_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                .add("allow_holding", SerializableDataTypes.BOOLEAN, true),
            data -> (powerType, livingEntity) -> new ClimbingPower(
                powerType,
                livingEntity,
                data.get("hold_condition"),
                data.get("allow_holding")
            )
        ).allowCondition();
    }
}
