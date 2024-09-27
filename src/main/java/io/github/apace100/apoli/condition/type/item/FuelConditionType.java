package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class FuelConditionType {

	public static boolean condition(ItemStack stack, Comparison comparison, int compareTo) {
		Integer fuelTime = FuelRegistry.INSTANCE.get(stack.getItem());
		return comparison.compare(fuelTime == null ? 0 : fuelTime, compareTo);
	}

	public static ConditionTypeFactory<Pair<World, ItemStack>> getFactory() {
		return new ConditionTypeFactory<>(
			Apoli.identifier("fuel"),
			new SerializableData()
				.add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
				.add("compare_to", SerializableDataTypes.INT, 0),
			(data, worldAndStack) -> condition(worldAndStack.getRight(),
				data.get("comparison"),
				data.get("compare_to")
			)
		);
	}

}