package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
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
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Optional;

public class BiomeConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Apoli.identifier("constant"), new SerializableData()
            .add("value", SerializableDataTypes.BOOLEAN),
            (data, biome) -> data.getBoolean("value")));
        register(new ConditionFactory<>(Apoli.identifier("and"), new SerializableData()
            .add("conditions", ApoliDataTypes.BIOME_CONDITIONS),
            (data, biome) -> ((List<ConditionFactory<RegistryEntry<Biome>>.Instance>)data.get("conditions")).stream().allMatch(
                condition -> condition.test(biome)
            )));
        register(new ConditionFactory<>(Apoli.identifier("or"), new SerializableData()
            .add("conditions", ApoliDataTypes.BIOME_CONDITIONS),
            (data, biome) -> ((List<ConditionFactory<RegistryEntry<Biome>>.Instance>)data.get("conditions")).stream().anyMatch(
                condition -> condition.test(biome)
            )));

        register(new ConditionFactory<>(Apoli.identifier("high_humidity"), new SerializableData(),
            (data, biome) -> biome.value().hasHighHumidity()));
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
            .add("precipitation", SerializableDataTypes.STRING),
            (data, biome) -> biome.value().getPrecipitation().getName().equals(data.getString("precipitation"))));
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
