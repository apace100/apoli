package io.github.apace100.apoli.loot.condition;

import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.Apoli;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ApoliLootConditionTypes {

	public static final LootConditionType POWER = register("power", PowerLootCondition.MAP_CODEC);

	public static void register() {

	}

	public static <C extends LootCondition> LootConditionType register(String path, MapCodec<C> mapCodec) {
		return Registry.register(Registries.LOOT_CONDITION_TYPE, Apoli.identifier(path), new LootConditionType(mapCodec));
	}

}
