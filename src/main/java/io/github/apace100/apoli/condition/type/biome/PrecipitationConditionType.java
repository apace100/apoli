package io.github.apace100.apoli.condition.type.biome;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class PrecipitationConditionType {

    public static boolean condition(BlockPos pos, RegistryEntry<Biome> biomeEntry, Biome.Precipitation precipitation) {
        return biomeEntry.value().getPrecipitation(pos) == precipitation;
    }

    public static ConditionTypeFactory<Pair<BlockPos, RegistryEntry<Biome>>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("precipitation"),
            new SerializableData()
                .add("precipitation", SerializableDataType.enumValue(Biome.Precipitation.class)),
            (data, posAndBiome) -> condition(posAndBiome.getLeft(), posAndBiome.getRight(),
                data.get("precipitation")
            )
        );
    }

}
