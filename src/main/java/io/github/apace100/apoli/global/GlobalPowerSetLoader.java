package io.github.apace100.apoli.global;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.PowerTypes;
import io.github.apace100.calio.data.IdentifiableMultiJsonDataLoader;
import io.github.apace100.calio.data.MultiJsonDataContainer;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;

import java.util.*;

public class GlobalPowerSetLoader extends IdentifiableMultiJsonDataLoader implements IdentifiableResourceReloadListener {

    public static final Identifier PHASE = Apoli.identifier("phase/global_powers");

    public static final Set<Identifier> DEPENDENCIES = Util.make(new HashSet<>(), set -> set.add(Apoli.identifier("powers")));
    public static final Map<Identifier, GlobalPowerSet> ALL = new HashMap<>();

    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();

    private static Identifier prevId = null;
    private static int prevPriority = Integer.MIN_VALUE;

    public GlobalPowerSetLoader() {
        super(GSON, "global_powers", ResourceType.SERVER_DATA);

        ServerEntityEvents.ENTITY_LOAD.addPhaseOrdering(PowerTypes.PHASE, PHASE);
        ServerEntityEvents.ENTITY_LOAD.register(PHASE, (entity, world) -> GlobalPowerSetUtil.applyGlobalPowers(entity));

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(PowerTypes.PHASE, PHASE);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(PHASE, (player, joined) -> {
            if (!joined) {
                GlobalPowerSetUtil.applyGlobalPowers(player);
            }
        });

    }

    @Override
    protected void apply(MultiJsonDataContainer prepared, ResourceManager manager, Profiler profiler) {

        ALL.clear();
        prevId = null;

        Map<Identifier, List<GlobalPowerSet>> loadedGlobalPowerSets = new HashMap<>();

        prepared.forEach((packName, id, jsonElement) -> {
            try {

                SerializableData.CURRENT_NAMESPACE = id.getNamespace();
                SerializableData.CURRENT_PATH = id.getPath();

                if (prevId == null || !prevId.equals(id)) {

                    prevPriority = Integer.MIN_VALUE;
                    prevId = id;

                }

                Apoli.LOGGER.info("Trying to read global power set file \"{}\" from data pack [{}]", id, packName);

                SerializableData.Instance data = GlobalPowerSet.DATA.read(jsonElement.getAsJsonObject());
                GlobalPowerSet globalPowerSet = GlobalPowerSet.FACTORY.fromData(data);

                int loadingPriority = globalPowerSet.getLoadingPriority();

                if (loadingPriority < prevPriority) {
                    Apoli.LOGGER.warn("Ignoring replaced duplicate global power set \"{}\" with a lower loading priority.", id);
                    return;
                }

                List<String> invalidPowers = globalPowerSet.validate()
                    .stream()
                    .map(pt -> pt.getIdentifier().toString())
                    .toList();

                if (!invalidPowers.isEmpty()) {
                    Apoli.LOGGER.error("Global power set \"{}\" (from data pack [{}]) contained {} invalid power(s): {}", id, packName, invalidPowers.size(), String.join(", ", invalidPowers));
                }

                List<GlobalPowerSet> globalPowerSets = loadedGlobalPowerSets.computeIfAbsent(id, k -> new LinkedList<>());

                if (globalPowerSet.shouldReplace()) {
                    globalPowerSets.clear();
                    prevPriority = loadingPriority + 1;
                }

                globalPowerSets.add(globalPowerSet);

            } catch (Exception e) {
                Apoli.LOGGER.error("There was a problem reading global power set file \"{}\" (skipping): {}", id, e.getMessage());
            }
        });

        Apoli.LOGGER.info("Finished loading global power sets. Merging similar global power sets...");
        loadedGlobalPowerSets.forEach((id, globalPowerSets) -> {

            GlobalPowerSet[] currentSet = {null};
            List<GlobalPowerSet> sortedSets = globalPowerSets
                .stream()
                .sorted(Comparator.comparing(GlobalPowerSet::getLoadingPriority))
                .toList();

            for (GlobalPowerSet set : sortedSets) {

                if (currentSet[0] == null) {
                    currentSet[0] = set;
                } else {
                    currentSet[0].merge(set);
                }

            }

            ALL.put(id, currentSet[0]);

        });

        Apoli.LOGGER.info("Finished merging similar global power sets from data files. Loaded {} global power sets.", ALL.size());

        SerializableData.CURRENT_NAMESPACE = null;
        SerializableData.CURRENT_PATH = null;

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
