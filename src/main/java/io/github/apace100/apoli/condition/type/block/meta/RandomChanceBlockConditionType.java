package io.github.apace100.apoli.condition.type.block.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.apoli.condition.type.meta.RandomChanceMetaConditionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RandomChanceBlockConditionType extends BlockConditionType implements RandomChanceMetaConditionType {

	private final float chance;

	public RandomChanceBlockConditionType(float chance) {
		this.chance = chance;
	}

	@Override
	public boolean test(World world, BlockPos pos) {
		return testCondition();
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return BlockConditionTypes.RANDOM_CHANCE;
	}

	@Override
	public float chance() {
		return chance;
	}

}
