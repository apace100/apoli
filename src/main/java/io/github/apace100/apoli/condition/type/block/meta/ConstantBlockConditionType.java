package io.github.apace100.apoli.condition.type.block.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.apoli.condition.type.meta.ConstantMetaConditionType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ConstantBlockConditionType extends BlockConditionType implements ConstantMetaConditionType {

	private final boolean value;

	public ConstantBlockConditionType(boolean value) {
		this.value = value;
	}

	@Override
	public boolean test(World world, BlockPos pos, BlockState blockState, Optional<BlockEntity> blockEntity) {
		return value();
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return BlockConditionTypes.CONSTANT;
	}

	@Override
	public boolean value() {
		return value;
	}

}
