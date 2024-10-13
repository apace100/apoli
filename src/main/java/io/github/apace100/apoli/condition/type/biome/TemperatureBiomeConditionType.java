package io.github.apace100.apoli.condition.type.biome;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BiomeConditionType;
import io.github.apace100.apoli.condition.type.BiomeConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class TemperatureBiomeConditionType extends BiomeConditionType {

    public static final DataObjectFactory<TemperatureBiomeConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
        data -> new TemperatureBiomeConditionType(
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final Comparison comparison;
    private final float compareTo;

    public TemperatureBiomeConditionType(Comparison comparison, float compareTo) {
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(BlockPos pos, RegistryEntry<Biome> biomeEntry) {
        return comparison.compare(biomeEntry.value().getTemperature(), compareTo);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BiomeConditionTypes.TEMPERATURE;
    }

}
