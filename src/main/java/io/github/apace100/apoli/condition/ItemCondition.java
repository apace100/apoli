package io.github.apace100.apoli.condition;

import io.github.apace100.apoli.condition.context.ItemContext;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemCondition extends AbstractCondition<ItemContext, ItemConditionType> {

	public static final SerializableDataType<ItemCondition> DATA_TYPE = SerializableDataType.lazy(() -> ApoliDataTypes.condition("type", ItemConditionTypes.DATA_TYPE, ItemCondition::new));

	public ItemCondition(ItemConditionType conditionType, boolean inverted) {
		super(conditionType, inverted);
	}

	public ItemCondition(ItemConditionType conditionType) {
		this(conditionType, false);
	}

	public boolean test(World world, ItemStack stack) {
		return test(new ItemContext(world, stack));
	}

}
