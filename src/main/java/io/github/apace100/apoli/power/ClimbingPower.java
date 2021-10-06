package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.function.Predicate;

public class ClimbingPower extends Power {

    private final boolean allowHolding;
    private final Predicate<Entity> holdingCondition;

    public ClimbingPower(PowerType<?> type, LivingEntity entity, boolean allowHolding, Predicate<Entity> holdingCondition) {
        super(type, entity);
        this.allowHolding = allowHolding;
        this.holdingCondition = holdingCondition;
    }

    public boolean canHold() {
        return allowHolding && (holdingCondition == null ? isActive() : holdingCondition.test(entity));
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("climbing"),
            new SerializableData()
                .add("allow_holding", SerializableDataTypes.BOOLEAN, true)
                .add("hold_condition", ApoliDataTypes.ENTITY_CONDITION, null),
            data ->
                (type, player) -> {
                    Predicate<Entity> holdCondition = (ConditionFactory<Entity>.Instance)data.get("hold_condition");
                    return new ClimbingPower(type, player, data.getBoolean("allow_holding"), holdCondition);
                })
            .allowCondition();
    }
}
