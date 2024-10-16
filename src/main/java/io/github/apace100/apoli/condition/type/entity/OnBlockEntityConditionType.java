package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class OnBlockEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<OnBlockEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty()),
        data -> new OnBlockEntityConditionType(
            data.get("block_condition")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("block_condition", conditionType.blockCondition)
    );

    private final Optional<BlockCondition> blockCondition;

    public OnBlockEntityConditionType(Optional<BlockCondition> blockCondition) {
        this.blockCondition = blockCondition;
    }

    @Override
    public boolean test(Entity entity) {
        BlockPos pos = BlockPos.ofFloored(entity.getX(), entity.getBoundingBox().minY - 0.5000001D, entity.getZ());
        return entity.isOnGround()
            && blockCondition.map(condition -> condition.test(entity.getWorld(), pos)).orElse(true);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.ON_BLOCK;
    }

}
