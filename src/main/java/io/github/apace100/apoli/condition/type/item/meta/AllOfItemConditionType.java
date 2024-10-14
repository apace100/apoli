package io.github.apace100.apoli.condition.type.item.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.condition.context.ItemContext;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.condition.type.meta.AllOfMetaConditionType;
import io.github.apace100.apoli.power.type.PowerType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class AllOfItemConditionType extends ItemConditionType implements AllOfMetaConditionType<ItemContext, ItemCondition> {

	private final List<ItemCondition> conditions;

	public AllOfItemConditionType(List<ItemCondition> conditions) {
		this.conditions = conditions;
	}

	@Override
	public boolean test(World world, ItemStack stack) {
		return AllOfMetaConditionType.condition(new ItemContext(world, stack), conditions());
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return ItemConditionTypes.ALL_OF;
	}

	@Override
	public List<ItemCondition> conditions() {
		return conditions;
	}

	@Override
	public void setPowerType(Optional<PowerType> powerType) {
		super.setPowerType(powerType);
		propagatePowerType(powerType);
	}

}
