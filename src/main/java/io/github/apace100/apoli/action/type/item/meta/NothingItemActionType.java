package io.github.apace100.apoli.action.type.item.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.action.type.meta.NothingMetaActionType;
import net.minecraft.inventory.StackReference;
import net.minecraft.world.World;

public class NothingItemActionType extends ItemActionType implements NothingMetaActionType {

	@Override
	public void execute(World world, StackReference stackReference) {

	}

	@Override
	public ActionConfiguration<?> configuration() {
		return ItemActionTypes.NOTHING;
	}

}
