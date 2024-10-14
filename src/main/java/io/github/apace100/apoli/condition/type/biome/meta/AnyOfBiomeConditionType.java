package io.github.apace100.apoli.condition.type.biome.meta;

import io.github.apace100.apoli.condition.BiomeCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BiomeContext;
import io.github.apace100.apoli.condition.type.BiomeConditionType;
import io.github.apace100.apoli.condition.type.BiomeConditionTypes;
import io.github.apace100.apoli.condition.type.meta.AnyOfMetaConditionType;
import io.github.apace100.apoli.power.type.PowerType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Optional;

public class AnyOfBiomeConditionType extends BiomeConditionType implements AnyOfMetaConditionType<BiomeContext, BiomeCondition> {

	private final List<BiomeCondition> conditions;

	public AnyOfBiomeConditionType(List<BiomeCondition> conditions) {
		this.conditions = conditions;
	}

	@Override
	public boolean test(BlockPos pos, RegistryEntry<Biome> biomeEntry) {
		return AnyOfMetaConditionType.condition(new BiomeContext(pos, biomeEntry), conditions());
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return BiomeConditionTypes.ANY_OF;
	}

	@Override
	public List<BiomeCondition> conditions() {
		return conditions;
	}

	@Override
	public void setPowerType(Optional<PowerType> powerType) {
		super.setPowerType(powerType);
		propagatePowerType(powerType);
	}

}
