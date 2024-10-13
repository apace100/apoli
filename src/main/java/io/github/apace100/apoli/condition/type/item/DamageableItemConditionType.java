package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class DamageableItemConditionType extends ItemConditionType {

	@Override
	public boolean test(World world, ItemStack stack) {
		return stack.isDamageable();
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return ItemConditionTypes.DAMAGEABLE;
	}

}
