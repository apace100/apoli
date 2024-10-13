package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EnchantableItemConditionType extends ItemConditionType {

	@Override
	public boolean test(World world, ItemStack stack) {
		return stack.isEnchantable();
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return ItemConditionTypes.ENCHANTABLE;
	}

}
