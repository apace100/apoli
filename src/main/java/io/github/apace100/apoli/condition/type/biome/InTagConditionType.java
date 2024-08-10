package io.github.apace100.apoli.condition.type.biome;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class InTagConditionType {

    public static boolean condition(RegistryEntry<Biome> biomeEntry, TagKey<Biome> biomeTag) {
        return biomeEntry.isIn(biomeTag);
    }

    public static ConditionTypeFactory<Pair<BlockPos, RegistryEntry<Biome>>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("in_tag"),
            new SerializableData()
                .add("tag", SerializableDataTypes.BIOME_TAG),
            (data, posAndBiome) -> condition(posAndBiome.getRight(),
                data.get("tag")
            )
        );
    }

}
