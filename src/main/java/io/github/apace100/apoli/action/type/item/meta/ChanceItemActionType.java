package io.github.apace100.apoli.action.type.item.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.action.type.meta.ChanceMetaActionType;
import net.minecraft.inventory.StackReference;
import net.minecraft.world.World;

import java.util.Optional;

public class ChanceItemActionType extends ItemActionType implements ChanceMetaActionType<ItemActionContext, ItemAction> {

	private final ItemAction successAction;
	private final Optional<ItemAction> failAction;

	private final float chance;

	public ChanceItemActionType(ItemAction successAction, Optional<ItemAction> failAction, float chance) {
		this.successAction = successAction;
		this.failAction = failAction;
		this.chance = chance;
	}

	@Override
	public void execute(World world, StackReference stackReference) {
		executeAction(new ItemActionContext(world, stackReference));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return ItemActionTypes.CHANCE;
	}

	@Override
	public ItemAction successAction() {
		return successAction;
	}

	@Override
	public Optional<ItemAction> failAction() {
		return failAction;
	}

	@Override
	public float chance() {
		return chance;
	}

}
