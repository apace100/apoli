package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.function.Predicate;

public class BlockCollisionCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        Box entityBoundingBox = entity.getBoundingBox();
        Box offsetEntityBoundingBox = entityBoundingBox.offset(
            data.getFloat("offset_x") * entityBoundingBox.getLengthX(),
            data.getFloat("offset_y") * entityBoundingBox.getLengthY(),
            data.getFloat("offset_z") * entityBoundingBox.getLengthZ()
        );

        if (data.isPresent("block_condition")) {

            Predicate<CachedBlockPosition> blockCondition = data.get("block_condition");
            BlockPos minBlockPos = BlockPos.ofFloored(offsetEntityBoundingBox.minX + 0.001, offsetEntityBoundingBox.minY + 0.001, offsetEntityBoundingBox.minZ + 0.001);
            BlockPos maxBlockPos = BlockPos.ofFloored(offsetEntityBoundingBox.maxX - 0.001, offsetEntityBoundingBox.maxY - 0.001, offsetEntityBoundingBox.maxZ - 0.001);
            BlockPos.Mutable mutableBlockPos = new BlockPos.Mutable();
            int matchingBlocks = 0;

            for (int x = minBlockPos.getX(); x <= maxBlockPos.getX(); x++) {
                for (int y = minBlockPos.getY(); y <= maxBlockPos.getY(); y++) {
                    for (int z = minBlockPos.getZ(); z <= maxBlockPos.getZ(); z++) {
                        mutableBlockPos.set(x, y, z);
                        if (blockCondition.test(new CachedBlockPosition(entity.getWorld(), mutableBlockPos, true))) {
                            matchingBlocks++;
                        }
                    }
                }
            }

            return matchingBlocks > 0;

        }

        else return entity.getWorld()
            .getBlockCollisions(entity, offsetEntityBoundingBox)
            .iterator()
            .hasNext();

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("block_collision"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("offset_x", SerializableDataTypes.FLOAT, 0F)
                .add("offset_y", SerializableDataTypes.FLOAT, 0F)
                .add("offset_z", SerializableDataTypes.FLOAT, 0F),
            BlockCollisionCondition::condition
        );
    }

}
