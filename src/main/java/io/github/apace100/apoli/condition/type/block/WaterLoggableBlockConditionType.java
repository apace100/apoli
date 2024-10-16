package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import net.minecraft.block.Waterloggable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WaterLoggableBlockConditionType extends BlockConditionType {

	@Override
	public boolean test(World world, BlockPos pos) {
		return world.getBlockState(pos).getBlock() instanceof Waterloggable;
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return BlockConditionTypes.WATER_LOGGABLE;
	}

}
