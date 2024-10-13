package io.github.apace100.apoli.condition.type.item.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.condition.type.meta.RandomChanceMetaConditionType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RandomChanceItemConditionType extends ItemConditionType implements RandomChanceMetaConditionType {

	private final float chance;

	public RandomChanceItemConditionType(float chance) {
		this.chance = chance;
	}

	@Override
	public boolean test(World world, ItemStack stack) {
		return RandomChanceMetaConditionType.condition(chance());
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return ItemConditionTypes.RANDOM_CHANCE;
	}

	@Override
	public float chance() {
		return chance;
	}

}
