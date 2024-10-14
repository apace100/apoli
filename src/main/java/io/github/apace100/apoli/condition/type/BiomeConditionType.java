package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.BiomeCondition;
import io.github.apace100.apoli.condition.context.BiomeContext;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public abstract class BiomeConditionType extends AbstractConditionType<BiomeContext, BiomeCondition> {

	@Override
	public final boolean test(BiomeContext context) {
		return test(context.pos(), context.biomeEntry());
	}

	public abstract boolean test(BlockPos pos, RegistryEntry<Biome> biomeEntry);

}