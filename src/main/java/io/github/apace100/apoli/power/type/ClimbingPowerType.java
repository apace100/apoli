package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.function.Predicate;

public class ClimbingPowerType extends PowerType {

    private final Predicate<Entity> holdingCondition;
    private final boolean allowHolding;

    public ClimbingPowerType(Power power, LivingEntity entity, Predicate<Entity> holdingCondition, boolean allowHolding) {
        super(power, entity);
        this.holdingCondition = holdingCondition;
        this.allowHolding = allowHolding;
    }

    public boolean canHold() {
        return allowHolding && this.shouldHold();
    }

    public boolean shouldHold() {
        return holdingCondition != null
            ? holdingCondition.test(entity)
            : entity.isSneaking();
    }

    public static PowerTypeFactory<ClimbingPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("climbing"),
            new SerializableData()
                .add("hold_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                .add("allow_holding", SerializableDataTypes.BOOLEAN, true),
            data -> (power, entity) -> new ClimbingPowerType(power, entity,
                data.get("hold_condition"),
                data.get("allow_holding")
            )
        ).allowCondition();
    }

}
