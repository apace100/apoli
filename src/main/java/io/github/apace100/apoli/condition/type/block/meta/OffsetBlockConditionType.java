package io.github.apace100.apoli.condition.type.block.meta;

import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class OffsetBlockConditionType extends BlockConditionType {

    public static final TypedDataObjectFactory<OffsetBlockConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("condition", BlockCondition.DATA_TYPE)
            .add("x", SerializableDataTypes.INT, 0)
            .add("y", SerializableDataTypes.INT, 0)
            .add("z", SerializableDataTypes.INT, 0)
            .addFunctionedDefault("offset", SerializableDataTypes.VECTOR, data -> new Vec3d(data.get("x"), data.get("y"), data.get("z"))),
        data -> new OffsetBlockConditionType(
            data.get("condition"),
            data.get("offset")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("condition", conditionType.blockCondition)
            .set("offset", conditionType.offset)
    );

    private final BlockCondition blockCondition;
    private final Vec3i offset;

    public OffsetBlockConditionType(BlockCondition blockCondition, Vec3i offset) {
        this.blockCondition = blockCondition;
        this.offset = offset;
    }

    @Override
    public boolean test(World world, BlockPos pos, BlockState blockState, Optional<BlockEntity> blockEntity) {

        BlockConditionContext context = offset.equals(Vec3i.ZERO)
            ? new BlockConditionContext(world, pos, blockState, blockEntity)
            : new BlockConditionContext(world, pos.add(offset));

        return blockCondition.test(context);

    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return BlockConditionTypes.OFFSET;
    }

}
