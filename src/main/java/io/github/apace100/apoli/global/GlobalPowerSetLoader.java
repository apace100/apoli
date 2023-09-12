package io.github.apace100.apoli.global;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.*;
import java.util.stream.Collectors;

public class GlobalPowerSetLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {

    public static final Set<Identifier> DEPENDENCIES = Set.of(Apoli.identifier("powers"));
    public static final List<GlobalPowerSet> ALL = new LinkedList<>();

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    public GlobalPowerSetLoader() {
        super(GSON, "global_powers");
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> GlobalPowerSetUtil.applyGlobalPowers(entity));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {

        ALL.clear();
        prepared.forEach((id, jsonElement) -> {

            if (!(jsonElement instanceof JsonObject jsonObject)) {
                return;
            }

            SerializableData.Instance data = GlobalPowerSet.DATA.read(jsonObject);
            GlobalPowerSet globalPowerSet = GlobalPowerSet.FACTORY.fromData(data);

            List<PowerType<?>> invalidPowerTypes = globalPowerSet.validate();
            if (!invalidPowerTypes.isEmpty()) {
                Apoli.LOGGER.error(
                    "Global power set \"{}\" contained invalid powers: {}",
                    id, invalidPowerTypes
                        .stream()
                        .map(PowerType::getIdentifier)
                        .map(Identifier::toString)
                        .collect(Collectors.joining(", "))
                );
            }

            ALL.add(globalPowerSet);

        });

        Apoli.LOGGER.info("Loaded {} global power sets.", ALL.size());

    }

    @Override
    public Identifier getFabricId() {
        return Apoli.identifier("global_powers");
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return DEPENDENCIES;
    }
}
