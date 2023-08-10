package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.BiomeWeatherAccess;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Optional;

public class BiomeConditions {

    public static void register() {
        MetaConditions.register(ApoliDataTypes.BIOME_CONDITION, BiomeConditions::register);
        register(new ConditionFactory<>(Apoli.identifier("high_humidity"), new SerializableData(),
            (data, biome) -> ((BiomeWeatherAccess)(Object)biome.value()).getDownfall() > 0.85f));
        register(new ConditionFactory<>(Apoli.identifier("temperature"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, biome) -> ((Comparison)data.get("comparison")).compare(biome.value().getTemperature(), data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("category"), new SerializableData() // Deprecated
            .add("category", SerializableDataTypes.STRING),
            (data, biome) -> {
                Identifier tagId = Apoli.identifier("category/" + data.getString("category"));
                TagKey<Biome> biomeTag = TagKey.of(RegistryKeys.BIOME, tagId);
                return biome.isIn(biomeTag);
            }));
        register(new ConditionFactory<>(Apoli.identifier("precipitation"), new SerializableData()
            .add("precipitation", SerializableDataType.enumValue(Biome.Precipitation.class)),
            (data, biome) -> biome.value().getPrecipitation(new BlockPos(0, 64, 0)).equals(data.get("precipitation"))));
        register(new ConditionFactory<>(Apoli.identifier("in_tag"), new SerializableData()
            .add("tag", SerializableDataTypes.BIOME_TAG),
            (data, biome) -> {
                TagKey<Biome> biomeTag = data.get("tag");
                return biome.isIn(biomeTag);
            }));
    }

    private static void register(ConditionFactory<RegistryEntry<Biome>> conditionFactory) {
        Registry.register(ApoliRegistries.BIOME_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
