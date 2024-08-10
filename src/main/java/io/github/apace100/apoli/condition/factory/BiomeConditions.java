package io.github.apace100.apoli.condition.factory;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.BiomeWeatherAccess;
import io.github.apace100.apoli.condition.type.biome.InTagConditionType;
import io.github.apace100.apoli.condition.type.biome.PrecipitationConditionType;
import io.github.apace100.apoli.condition.type.biome.TemperatureConditionType;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.function.Predicate;

public class BiomeConditions {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();

    public static void register() {
        MetaConditions.register(ApoliDataTypes.BIOME_CONDITION, BiomeConditions::register);
        register(createSimpleFactory(Apoli.identifier("high_humidity"), biomeEntry -> ((BiomeWeatherAccess) (Object) biomeEntry.value()).apoli$getDownfall() > 0.85F));
        register(TemperatureConditionType.getFactory());
        register(PrecipitationConditionType.getFactory());
        register(InTagConditionType.getFactory());
    }

    public static ConditionTypeFactory<Pair<BlockPos, RegistryEntry<Biome>>> createSimpleFactory(Identifier id, Predicate<RegistryEntry<Biome>> condition) {
        return new ConditionTypeFactory<>(id, new SerializableData(), (data, posAndBiome) -> condition.test(posAndBiome.getRight()));
    }

    public static <F extends ConditionTypeFactory<Pair<BlockPos, RegistryEntry<Biome>>>> F register(F conditionFactory) {
        return Registry.register(ApoliRegistries.BIOME_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }

}
