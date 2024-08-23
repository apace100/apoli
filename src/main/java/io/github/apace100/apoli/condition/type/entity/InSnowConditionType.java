package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.util.WorldUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class InSnowConditionType {

	public static boolean condition(Entity entity) {

		BlockPos downBlockPos = entity.getBlockPos();
		BlockPos upBlockPos = BlockPos.ofFloored(downBlockPos.getX(), entity.getBoundingBox().maxY, downBlockPos.getZ());

		return WorldUtil.inSnow(entity.getWorld(), downBlockPos, upBlockPos);

	}

}
