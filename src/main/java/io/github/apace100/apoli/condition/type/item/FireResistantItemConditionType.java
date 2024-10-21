package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class FireResistantItemConditionType extends ItemConditionType {

	@Override
	public boolean test(World world, ItemStack stack) {
		return stack.contains(DataComponentTypes.FIRE_RESISTANT);
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return ItemConditionTypes.FIRE_RESISTANT;
	}

}
