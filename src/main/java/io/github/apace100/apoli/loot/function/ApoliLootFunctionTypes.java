package io.github.apace100.apoli.loot.function;

import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.Apoli;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ApoliLootFunctionTypes {

	public static final LootFunctionType<AddPowerLootFunction> ADD_POWER = register("add_power", AddPowerLootFunction.MAP_CODEC);
	public static final LootFunctionType<RemovePowerLootFunction> REMOVE_POWER = register("remove_power", RemovePowerLootFunction.MAP_CODEC);

	public static void register() {

	}

	public static <F extends LootFunction> LootFunctionType<F> register(String path, MapCodec<F> mapCodec) {
		return Registry.register(Registries.LOOT_FUNCTION_TYPE, Apoli.identifier(path), new LootFunctionType<>(mapCodec));
	}

}
