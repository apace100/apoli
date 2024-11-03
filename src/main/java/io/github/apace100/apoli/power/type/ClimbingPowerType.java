package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.type.entity.SneakingEntityConditionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ClimbingPowerType extends PowerType {

    public static final TypedDataObjectFactory<ClimbingPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .addSupplied("holding_condition", EntityCondition.DATA_TYPE, () -> new SneakingEntityConditionType().createCondition())
            .add("allow_holding", SerializableDataTypes.BOOLEAN, true),
        (data, condition) -> new ClimbingPowerType(
            data.get("holding_condition"),
            data.get("allow_holding"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("holding_condition", powerType.holdingCondition)
            .set("allow_holding", powerType.allowHolding)
    );

    private final EntityCondition holdingCondition;
    private final boolean allowHolding;

    public ClimbingPowerType(EntityCondition holdingCondition, boolean allowHolding, Optional<EntityCondition> condition) {
        super(condition);
        this.holdingCondition = holdingCondition;
        this.allowHolding = allowHolding;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.CLIMBING;
    }

    public boolean canHold() {
        return allowHolding && holdingCondition.test(getHolder());
    }

}
