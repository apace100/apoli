package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class FuelCondition {

	public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {

		Integer fuelTime = FuelRegistry.INSTANCE.get(worldAndStack.getRight().getItem());
		Comparison comparison = data.get("comparison");
		int compareTo = data.get("compare_to");

		return fuelTime != null
			&& comparison.compare(fuelTime, compareTo);

	}

	public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
		return new ConditionFactory<>(
			Apoli.identifier("fuel"),
			new SerializableData()
				.add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
				.add("compare_to", SerializableDataTypes.INT, 0),
			FuelCondition::condition
		);
	}

}
