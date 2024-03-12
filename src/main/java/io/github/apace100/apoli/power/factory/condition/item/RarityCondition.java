package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

public class RarityCondition {
	public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {
		return worldAndStack.getRight().getRarity().equals((Rarity)data.get("rarity"));
	}

	public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
		return new ConditionFactory<>(
			Apoli.identifier("rarity"),
			new SerializableData()
				.add("rarity", ApoliDataTypes.RARITY),
			RarityCondition::condition
		);
	}
}
