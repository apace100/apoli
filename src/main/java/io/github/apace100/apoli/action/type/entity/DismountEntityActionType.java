package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class DismountEntityActionType extends EntityActionType {

	@Override
	protected void execute(Entity entity) {
		entity.stopRiding();
	}

	@Override
	public @NotNull ActionConfiguration<?> getConfig() {
		return EntityActionTypes.DISMOUNT;
	}

}
