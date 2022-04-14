package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.mixin.BiomeAccessor;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.util.List;

public class BiomeConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Apoli.identifier("constant"), new SerializableData()
            .add("value", SerializableDataTypes.BOOLEAN),
            (data, fluid) -> data.getBoolean("value")));
        register(new ConditionFactory<>(Apoli.identifier("and"), new SerializableData()
            .add("conditions", ApoliDataTypes.BIOME_CONDITIONS),
            (data, fluid) -> ((List<ConditionFactory<Biome>.Instance>)data.get("conditions")).stream().allMatch(
                condition -> condition.test(fluid)
            )));
        register(new ConditionFactory<>(Apoli.identifier("or"), new SerializableData()
            .add("conditions", ApoliDataTypes.BIOME_CONDITIONS),
            (data, fluid) -> ((List<ConditionFactory<Biome>.Instance>)data.get("conditions")).stream().anyMatch(
                condition -> condition.test(fluid)
            )));

        register(new ConditionFactory<>(Apoli.identifier("high_humidity"), new SerializableData(),
            (data, biome) -> biome.hasHighHumidity()));
        register(new ConditionFactory<>(Apoli.identifier("temperature"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, biome) -> ((Comparison)data.get("comparison")).compare(biome.getTemperature(), data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("category"), new SerializableData()
            .add("category", SerializableDataTypes.STRING),
            (data, biome) -> ((BiomeAccessor)(Object)biome).getCategory().getName().equals(data.getString("category"))));
        register(new ConditionFactory<>(Apoli.identifier("precipitation"), new SerializableData()
            .add("precipitation", SerializableDataTypes.STRING),
            (data, biome) -> biome.getPrecipitation().getName().equals(data.getString("precipitation"))));
    }

    private static void register(ConditionFactory<Biome> conditionFactory) {
        Registry.register(ApoliRegistries.BIOME_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
