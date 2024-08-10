package io.github.apace100.apoli.condition.type.biome;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class TemperatureConditionType {

    public static boolean condition(RegistryEntry<Biome> biomeEntry, Comparison comparison, float compareTo) {
        return comparison.compare(biomeEntry.value().getTemperature(), compareTo);
    }

    public static ConditionTypeFactory<Pair<BlockPos, RegistryEntry<Biome>>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("temperature"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.FLOAT),
            (data, posAndBiome) -> condition(posAndBiome.getRight(),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
