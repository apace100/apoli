package io.github.apace100.apoli.condition.type.block.meta;

import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class OffsetBlockConditionType extends BlockConditionType {

    public static final DataObjectFactory<OffsetBlockConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("condition", BlockCondition.DATA_TYPE)
            .add("x", SerializableDataTypes.INT, 0)
            .add("y", SerializableDataTypes.INT, 0)
            .add("z", SerializableDataTypes.INT, 0),
        data -> new OffsetBlockConditionType(
            data.get("condition"),
            new Vec3i(
                data.get("x"),
                data.get("y"),
                data.get("z")
            )
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("condition", conditionType.blockCondition)
            .set("x", conditionType.offset.getX())
            .set("y", conditionType.offset.getY())
            .set("z", conditionType.offset.getZ())
    );

    private final BlockCondition blockCondition;
    private final Vec3i offset;

    public OffsetBlockConditionType(BlockCondition blockCondition, Vec3i offset) {
        this.blockCondition = blockCondition;
        this.offset = offset;
    }

    @Override
    public boolean test(World world, BlockPos pos) {
        return blockCondition.test(world, pos.add(offset));
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BlockConditionTypes.OFFSET;
    }

}
