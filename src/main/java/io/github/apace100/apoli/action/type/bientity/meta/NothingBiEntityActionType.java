package io.github.apace100.apoli.action.type.bientity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.action.type.meta.NothingMetaActionType;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class NothingBiEntityActionType extends BiEntityActionType implements NothingMetaActionType {

	@Override
	protected void execute(Entity actor, Entity target) {

	}

	@Override
	public @NotNull ActionConfiguration<?> getConfig() {
		return BiEntityActionTypes.NOTHING;
	}

}
