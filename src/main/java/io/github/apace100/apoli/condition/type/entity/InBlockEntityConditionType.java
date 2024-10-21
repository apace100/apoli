package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class InBlockEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<InBlockEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("block_condition", ApoliDataTypes.BLOCK_CONDITION),
        data -> new InBlockEntityConditionType(
            data.get("block_condition")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("block_condition", conditionType.blockCondition)
    );

    private final BlockCondition blockCondition;

    public InBlockEntityConditionType(BlockCondition blockCondition) {
        this.blockCondition = blockCondition;
    }

    @Override
    public boolean test(Entity entity) {
        return blockCondition.test(entity.getWorld(), entity.getBlockPos());
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.IN_BLOCK;
    }

}
