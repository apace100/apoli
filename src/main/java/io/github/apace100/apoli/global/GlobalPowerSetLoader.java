package io.github.apace100.apoli.global;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.MultiJsonDataLoader;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.registry.Registry;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.*;

public class GlobalPowerSetLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {

    public static final Set<Identifier> DEPENDENCIES = Set.of(Apoli.identifier("powers"));

    public static List<GlobalPowerSet> ALL = new LinkedList<>();

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    public GlobalPowerSetLoader() {
        super(GSON, "global_powers");

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            GlobalPowerSetUtil.applyGlobalPowers(entity);
        });
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        ALL.clear();
        prepared.forEach((id, json) -> {
            if(json.isJsonObject()) {
                SerializableData.Instance data = GlobalPowerSet.DATA.read(json.getAsJsonObject());
                GlobalPowerSet gps = GlobalPowerSet.FACTORY.fromData(data);
                ALL.add(gps);
            }
        });
        Apoli.LOGGER.info("Loaded " + ALL.size() + " global power sets.");
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
