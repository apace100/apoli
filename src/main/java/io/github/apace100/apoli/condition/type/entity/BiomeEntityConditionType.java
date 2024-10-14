package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.BiomeCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Optional;

public class BiomeEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<BiomeEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("condition", BiomeCondition.DATA_TYPE.optional(), Optional.empty())
            .add("biome", SerializableDataType.registryKey(RegistryKeys.BIOME).optional(), Optional.empty())
            .add("biomes", SerializableDataType.registryKey(RegistryKeys.BIOME).list().optional(), Optional.empty()),
        data -> new BiomeEntityConditionType(
            data.get("condition"),
            data.get("biome"),
            data.get("biomes")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("condition", conditionType.biomeCondition)
            .set("biome", conditionType.biome)
            .set("biomes", conditionType.biomes)
    );

    private final Optional<BiomeCondition> biomeCondition;

    private final Optional<RegistryKey<Biome>> biome;
    private final Optional<List<RegistryKey<Biome>>> biomes;

    public BiomeEntityConditionType(Optional<BiomeCondition> biomeCondition, Optional<RegistryKey<Biome>> biome, Optional<List<RegistryKey<Biome>>> biomes) {
        this.biomeCondition = AbstractCondition.setPowerType(biomeCondition, getPowerType());
        this.biome = biome;
        this.biomes = biomes;
    }

    @Override
    public boolean test(Entity entity) {

        RegistryEntry<Biome> biomeEntry = entity.getWorld().getBiome(entity.getBlockPos());
        RegistryKey<Biome> biomeKey = biomeEntry.getKey().orElseThrow();

        return biome.map(biomeKey::equals).orElse(true)
            && biomes.map(keys -> keys.contains(biomeKey)).orElse(true)
            && biomeCondition.map(condition -> condition.test(entity.getBlockPos(), biomeEntry)).orElse(true);

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.BIOME;
    }

}
