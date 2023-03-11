package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.Registry;

import java.util.List;

public class LoadingConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new LoadingConditionFactory(Apoli.identifier("constant"), new SerializableData()
                .add("value", SerializableDataTypes.BOOLEAN),
                (data) -> data.getBoolean("value")));
        register(new LoadingConditionFactory(Apoli.identifier("and"), new SerializableData()
                .add("conditions", ApoliDataTypes.LOADING_CONDITIONS),
                (data) -> ((List<LoadingConditionFactory.Instance>)data.get("conditions")).stream().allMatch(
                        LoadingConditionFactory.Instance::test
                )));
        register(new LoadingConditionFactory(Apoli.identifier("or"), new SerializableData()
                .add("conditions", ApoliDataTypes.LOADING_CONDITIONS),
                (data) -> ((List<LoadingConditionFactory.Instance>)data.get("conditions")).stream().anyMatch(
                        LoadingConditionFactory.Instance::test
                )));
        register(new LoadingConditionFactory(Apoli.identifier("mod_present"), new SerializableData()
                .add("id", SerializableDataTypes.STRING),
                (data) -> FabricLoader.getInstance().isModLoaded(data.getString("id"))));
        register(new LoadingConditionFactory(Apoli.identifier("datapack_present"), new SerializableData()
                .add("namespace", SerializableDataTypes.STRING),
                (data) -> PowerTypes.LOADED_DATAPACK_NAMESPACES.contains(data.getString("namespace"))));
    }

    private static void register(LoadingConditionFactory conditionFactory) {
        Registry.register(ApoliRegistries.LOADING_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
