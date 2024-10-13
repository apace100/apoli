package io.github.apace100.apoli.condition.type.item.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.condition.type.meta.ConstantMetaConditionType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ConstantItemConditionType extends ItemConditionType implements ConstantMetaConditionType {

	private final boolean value;

	public ConstantItemConditionType(boolean value) {
		this.value = value;
	}

	@Override
	public boolean test(World world, ItemStack stack) {
		return value();
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return ItemConditionTypes.CONSTANT;
	}

	@Override
	public boolean value() {
		return value;
	}

}
