package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.util.WorldUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class InSnowEntityConditionType extends EntityConditionType {

	@Override
	public boolean test(Entity entity) {
		return WorldUtil.inSnow(entity.getWorld(), BlockPos.ofFloored(entity.getEyePos()), entity.getBlockPos());
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return EntityConditionTypes.IN_SNOW;
	}

}
