package io.github.apace100.apoli.condition.type.biome;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BiomeConditionType;
import io.github.apace100.apoli.condition.type.BiomeConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class PrecipitationBiomeConditionType extends BiomeConditionType {

    public static final TypedDataObjectFactory<PrecipitationBiomeConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("precipitation", SerializableDataType.enumValue(Biome.Precipitation.class)),
        data -> new PrecipitationBiomeConditionType(
            data.get("precipitation")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("precipitation", conditionType.precipitation)
    );

    private final Biome.Precipitation precipitation;

    public PrecipitationBiomeConditionType(Biome.Precipitation precipitation) {
        this.precipitation = precipitation;
    }

    @Override
    public boolean test(BlockPos pos, RegistryEntry<Biome> biomeEntry) {
        return biomeEntry.value().getPrecipitation(pos) == precipitation;
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BiomeConditionTypes.PRECIPITATION;
    }

}
