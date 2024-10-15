package io.github.apace100.apoli.condition;

import io.github.apace100.apoli.condition.context.BiomeConditionContext;
import io.github.apace100.apoli.condition.type.BiomeConditionType;
import io.github.apace100.apoli.condition.type.BiomeConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class BiomeCondition extends AbstractCondition<BiomeConditionContext, BiomeConditionType> {

	public static final SerializableDataType<BiomeCondition> DATA_TYPE = SerializableDataType.lazy(() -> ApoliDataTypes.condition("type", BiomeConditionTypes.DATA_TYPE, BiomeCondition::new));

	public BiomeCondition(BiomeConditionType conditionType, boolean inverted) {
		super(conditionType, inverted);
	}

	public BiomeCondition(BiomeConditionType conditionType) {
		this(conditionType, false);
	}

	public boolean test(BlockPos pos, RegistryEntry<Biome> biomeEntry) {
		return test(new BiomeConditionContext(pos, biomeEntry));
	}

}
