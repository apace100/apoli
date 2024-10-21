package io.github.apace100.apoli.condition.type.biome.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BiomeConditionType;
import io.github.apace100.apoli.condition.type.BiomeConditionTypes;
import io.github.apace100.apoli.condition.type.meta.ConstantMetaConditionType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class ConstantBiomeConditionType extends BiomeConditionType implements ConstantMetaConditionType {

	private final boolean value;

	public ConstantBiomeConditionType(boolean value) {
		this.value = value;
	}

	@Override
	public boolean test(BlockPos pos, RegistryEntry<Biome> biomeEntry) {
		return value();
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return BiomeConditionTypes.CONSTANT;
	}

	@Override
	public boolean value() {
		return value;
	}

}
