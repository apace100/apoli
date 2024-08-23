package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.BlockCollisionSpliteratorAccess;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class BlockCollisionConditionType {

    public static boolean condition(Entity entity, Predicate<CachedBlockPosition> blockCondition, Vec3d offset) {

        Box boundingBox = entity.getBoundingBox().offset(offset);
        World world = entity.getWorld();

        BlockCollisionSpliterator<BlockPos> spliterator = new BlockCollisionSpliterator<>(world, entity, boundingBox, false, (pos, shape) -> pos);
        ((BlockCollisionSpliteratorAccess) spliterator).apoli$setGetOriginalShapes(true);

        while (spliterator.hasNext()) {

            BlockPos pos = spliterator.next();

            if (blockCondition.test(new CachedBlockPosition(world, pos, true))) {
                return true;
            }

        }

        return false;

    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("block_collision"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("offset_x", SerializableDataTypes.DOUBLE, 0.0)
                .add("offset_y", SerializableDataTypes.DOUBLE, 0.0)
                .add("offset_z", SerializableDataTypes.DOUBLE, 0.0),
            (data, entity) -> condition(entity,
                data.getOrElse("block_condition", cachedBlock -> true),
                new Vec3d(data.get("offset_x"), data.get("offset_y"), data.get("offset_z"))
            )
        );
    }

}
