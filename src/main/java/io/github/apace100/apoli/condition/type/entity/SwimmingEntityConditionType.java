package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class SwimmingEntityConditionType extends EntityConditionType {

	@Override
	public boolean test(Entity entity) {
		return entity.isSwimming();
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return EntityConditionTypes.SWIMMING;
	}

}
