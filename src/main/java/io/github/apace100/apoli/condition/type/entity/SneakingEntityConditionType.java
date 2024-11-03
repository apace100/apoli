package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class SneakingEntityConditionType extends EntityConditionType {

	@Override
	public boolean test(Entity entity) {
		return entity.isSneaking();
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return EntityConditionTypes.SNEAKING;
	}

}
