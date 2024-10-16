package io.github.apace100.apoli.condition.type.biome;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BiomeConditionType;
import io.github.apace100.apoli.condition.type.BiomeConditionTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class HighHumidityBiomeConditionType extends BiomeConditionType {

	@Override
	public boolean test(BlockPos pos, RegistryEntry<Biome> biomeEntry) {
		return biomeEntry.value().weather.downfall() > 0.85F;
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return BiomeConditionTypes.HIGH_HUMIDITY;
	}

}
