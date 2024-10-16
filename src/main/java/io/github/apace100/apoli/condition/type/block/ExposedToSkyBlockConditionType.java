package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExposedToSkyBlockConditionType extends BlockConditionType {

	@Override
	public boolean test(World world, BlockPos pos) {
		return world.isSkyVisible(pos);
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return BlockConditionTypes.EXPOSED_TO_SKY;
	}

}
