package io.github.apace100.apoli.condition;

import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockCondition extends AbstractCondition<BlockConditionContext, BlockConditionType> {

	public static final SerializableDataType<BlockCondition> DATA_TYPE = SerializableDataType.lazy(() -> ApoliDataTypes.condition("type", BlockConditionTypes.DATA_TYPE, BlockCondition::new));

	public BlockCondition(BlockConditionType conditionType, boolean inverted) {
		super(conditionType, inverted);
	}

	public BlockCondition(BlockConditionType conditionType) {
		this(conditionType, false);
	}

	@Override
	public boolean test(BlockConditionContext operand) {
		return operand.world().isChunkLoaded(operand.pos())
			&& super.test(operand);
	}

	public boolean test(World world, BlockPos pos) {
		return test(new BlockConditionContext(world, pos));
	}

}
