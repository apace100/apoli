package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class BiomeConditionType {

    public static boolean condition(Entity entity, @Nullable Predicate<RegistryEntry<Biome>> biomeCondition, Collection<RegistryKey<Biome>> specBiomeKeys) {

        RegistryEntry<Biome> biomeEntry = entity.getWorld().getBiome(entity.getBlockPos());
        RegistryKey<Biome> biomeKey = biomeEntry.getKey().orElseThrow();

        return (!specBiomeKeys.isEmpty() && specBiomeKeys.contains(biomeKey))
            && (biomeCondition != null && biomeCondition.test(biomeEntry));

    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("biome"),
            new SerializableData()
                .add("condition", ApoliDataTypes.BIOME_CONDITION, null)
                .add("biome", SerializableDataType.registryKey(RegistryKeys.BIOME), null)
                .add("biomes", SerializableDataType.registryKey(RegistryKeys.BIOME).listOf(), null),
            (data, entity) -> {

                Set<RegistryKey<Biome>> specBiomeKeys = new HashSet<>();

                data.ifPresent("biome", specBiomeKeys::add);
                data.ifPresent("biomes", specBiomeKeys::addAll);

                return condition(entity,
                    data.get("condition"),
                    specBiomeKeys
                );

            }
        );
    }

}
