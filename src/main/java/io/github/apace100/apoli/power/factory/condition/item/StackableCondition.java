package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class StackableCondition {
	public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {
		return worldAndStack.getRight().isStackable();
	}

	public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
		return new ConditionFactory<>(
			Apoli.identifier("stackable"),
			new SerializableData(),
			StackableCondition::condition
		);
	}
}
