package io.github.apace100.apoli.condition.type.biome;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BiomeConditionType;
import io.github.apace100.apoli.condition.type.BiomeConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class InTagBiomeConditionType extends BiomeConditionType {

    public static final DataObjectFactory<InTagBiomeConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("tag", SerializableDataTypes.BIOME_TAG),
        data -> new InTagBiomeConditionType(
            data.get("tag")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("tag", conditionType.tag)
    );

    private final TagKey<Biome> tag;

    public InTagBiomeConditionType(TagKey<Biome> tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(BlockPos pos, RegistryEntry<Biome> biomeEntry) {
        return biomeEntry.isIn(tag);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BiomeConditionTypes.IN_TAG;
    }

}
