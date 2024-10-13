package io.github.apace100.apoli.condition.type.biome.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BiomeConditionType;
import io.github.apace100.apoli.condition.type.BiomeConditionTypes;
import io.github.apace100.apoli.condition.type.meta.RandomChanceMetaConditionType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class RandomChanceBiomeConditionType extends BiomeConditionType implements RandomChanceMetaConditionType {

	private final float chance;

	public RandomChanceBiomeConditionType(float chance) {
		this.chance = chance;
	}

	@Override
	public boolean test(BlockPos pos, RegistryEntry<Biome> biomeEntry) {
		return RandomChanceMetaConditionType.condition(chance());
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return BiomeConditionTypes.RANDOM_CHANCE;
	}

	@Override
	public float chance() {
		return chance;
	}

}
