package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.BlockCollisionSpliteratorAccess;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class BlockCollisionCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        Box entityBoundingBox = entity.getBoundingBox();
        Box offsetEntityBoundingBox = entityBoundingBox.offset(
            data.getFloat("offset_x") * entityBoundingBox.getLengthX(),
            data.getFloat("offset_y") * entityBoundingBox.getLengthY(),
            data.getFloat("offset_z") * entityBoundingBox.getLengthZ()
        );

        Predicate<CachedBlockPosition> blockCondition = data.get("block_condition");
        World world = entity.getWorld();

        BlockCollisionSpliterator<BlockPos> spliterator = new BlockCollisionSpliterator<>(entity.getWorld(), entity, offsetEntityBoundingBox, false, (pos, shape) -> pos);
        ((BlockCollisionSpliteratorAccess) spliterator).apoli$setGetOriginalShapes(true);

        while (spliterator.hasNext()) {

            BlockPos blockPos = spliterator.next();

            if (blockCondition == null || blockCondition.test(new CachedBlockPosition(world, blockPos, true))) {
                return true;
            }

        }

        return false;

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
