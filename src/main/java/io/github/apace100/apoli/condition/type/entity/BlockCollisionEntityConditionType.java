package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.access.BlockCollisionSpliteratorAccess;
import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.World;

import java.util.Optional;

public class BlockCollisionEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<BlockCollisionEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("offset_x", SerializableDataTypes.DOUBLE, 0.0)
            .add("offset_y", SerializableDataTypes.DOUBLE, 0.0)
            .add("offset_z", SerializableDataTypes.DOUBLE, 0.0),
        data -> new BlockCollisionEntityConditionType(
            data.get("block_collision"),
            new Vec3d(
                data.get("offset_x"),
                data.get("offset_y"),
                data.get("offset_z")
            )
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("block_condition", conditionType.blockCondition)
            .set("offset_x", conditionType.offset.getX())
            .set("offset_y", conditionType.offset.getY())
            .set("offset_z", conditionType.offset.getZ())
    );

    private final Optional<BlockCondition> blockCondition;
    private final Vec3d offset;

    public BlockCollisionEntityConditionType(Optional<BlockCondition> blockCondition, Vec3d offset) {
        this.blockCondition = AbstractCondition.setPowerType(blockCondition, getPowerType());
        this.offset = offset;
    }

    @Override
    public boolean test(Entity entity) {

        Box boundingBox = entity.getBoundingBox().offset(offset);
        World world = entity.getWorld();

        BlockCollisionSpliterator<BlockPos> spliterator = new BlockCollisionSpliterator<>(world, entity, boundingBox, false, (pos, shape) -> pos);
        ((BlockCollisionSpliteratorAccess) spliterator).apoli$setGetOriginalShapes(true);

        while (spliterator.hasNext()) {

            BlockPos pos = spliterator.next();

            if (blockCondition.map(condition -> condition.test(world, pos)).orElse(true)) {
                return true;
            }

        }

        return false;

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.BLOCK_COLLISION;
    }

}
