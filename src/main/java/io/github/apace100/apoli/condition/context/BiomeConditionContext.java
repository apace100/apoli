package io.github.apace100.apoli.condition.context;

import io.github.apace100.apoli.util.context.TypeConditionContext;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public record BiomeConditionContext(BlockPos pos, RegistryEntry<Biome> biomeEntry) implements TypeConditionContext {

}
