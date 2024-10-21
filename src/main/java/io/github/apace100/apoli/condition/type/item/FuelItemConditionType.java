package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Optional;

public class FuelItemConditionType extends ItemConditionType {

	public static final TypedDataObjectFactory<FuelItemConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
		new SerializableData()
			.add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
			.add("compare_to", SerializableDataTypes.INT, 0),
		data -> new FuelItemConditionType(
			data.get("comparison"),
			data.get("compare_to")
		),
		(conditionType, serializableData) -> serializableData.instance()
			.set("comparison", conditionType.comparison)
			.set("compare_to", conditionType.compareTo)
	);

	private final Comparison comparison;
	private final int compareTo;

	public FuelItemConditionType(Comparison comparison, int compareTo) {
		this.comparison = comparison;
		this.compareTo = compareTo;
	}

	@Override
	public boolean test(World world, ItemStack stack) {
		return Optional.ofNullable(FuelRegistry.INSTANCE.get(stack.getItem()))
			.map(value -> comparison.compare(value, compareTo))
			.orElse(false);
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return ItemConditionTypes.FUEL;
	}

}
