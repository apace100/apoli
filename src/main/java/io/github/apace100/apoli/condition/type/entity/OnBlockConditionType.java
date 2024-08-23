package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public class OnBlockConditionType {

    public static boolean condition(Entity entity, Predicate<CachedBlockPosition> blockCondition) {
        BlockPos pos = BlockPos.ofFloored(entity.getX(), entity.getBoundingBox().minY - 0.5000001D, entity.getY());
        return entity.isOnGround()
            && blockCondition.test(new CachedBlockPosition(entity.getWorld(), pos, true));
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("on_block"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null),
            (data, entity) -> condition(entity,
                data.getOrElse("block_condition", cachedBlock -> true)
            )
        );
    }

}
