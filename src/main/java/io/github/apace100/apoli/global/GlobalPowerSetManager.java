package io.github.apace100.apoli.global;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.util.PrioritizedEntry;
import io.github.apace100.calio.CalioServer;
import io.github.apace100.calio.data.IdentifiableMultiJsonDataLoader;
import io.github.apace100.calio.data.MultiJsonDataContainer;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.TagLike;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 *  TODO: Implement an immutable storage structure like {@link PowerManager} -eggohito
 */
public class GlobalPowerSetManager extends IdentifiableMultiJsonDataLoader implements IdentifiableResourceReloadListener {

    public static final Identifier PHASE = Apoli.identifier("phase/global_powers");
    public static final Set<Identifier> DEPENDENCIES = Util.make(new HashSet<>(), set -> set.add(Apoli.identifier("powers")));

    public static final Set<Identifier> DISABLED = new HashSet<>();
    public static final Map<Identifier, GlobalPowerSet> ALL = new HashMap<>();

    private static final Map<Identifier, Integer> LOADING_PRIORITIES = new HashMap<>();
    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();

    public GlobalPowerSetManager() {
        super(GSON, "global_powers", ResourceType.SERVER_DATA);

        ServerEntityEvents.ENTITY_LOAD.addPhaseOrdering(PowerManager.PHASE, PHASE);
        ServerEntityEvents.ENTITY_LOAD.register(PHASE, (entity, world) -> GlobalPowerSetUtil.applyGlobalPowers(entity));

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(PowerManager.PHASE, PHASE);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(PHASE, (player, joined) -> {
            if (!joined) {
                GlobalPowerSetUtil.applyGlobalPowers(player);
            }
        });

    }

    @Override
    protected void apply(MultiJsonDataContainer prepared, ResourceManager manager, Profiler profiler) {

        Apoli.LOGGER.info("Reading global power sets from data packs...");
        LOADING_PRIORITIES.clear();

        DISABLED.clear();
        ALL.clear();

        Map<Identifier, List<PrioritizedEntry<GlobalPowerSet>>> loadedGlobalPowerSets = new Object2ObjectLinkedOpenHashMap<>();
        DynamicRegistryManager dynamicRegistries = CalioServer.getDynamicRegistries().orElse(null);

        if (dynamicRegistries == null) {
            Apoli.LOGGER.error("Can't read global power sets from data packs without access to dynamic registries!");
            return;
        }

        prepared.forEach((packName, id, jsonElement) -> {

            try {

                SerializableData.CURRENT_NAMESPACE = id.getNamespace();
                SerializableData.CURRENT_PATH = id.getPath();

                if (!(jsonElement instanceof JsonObject jsonObject)) {
                    throw new JsonSyntaxException("Not a JSON object: " + jsonElement);
                }

                GlobalPowerSet globalPowerSet = GlobalPowerSet.DATA_TYPE.read(dynamicRegistries.getOps(JsonOps.INSTANCE), jsonObject).getOrThrow();
                int currLoadingPriority = JsonHelper.getInt(jsonObject, "loading_priority", 0);

                PrioritizedEntry<GlobalPowerSet> entry = new PrioritizedEntry<>(globalPowerSet, currLoadingPriority);
                int prevLoadingPriority = LOADING_PRIORITIES.getOrDefault(id, Integer.MIN_VALUE);

                if (globalPowerSet.shouldReplace() && currLoadingPriority <= prevLoadingPriority) {
                    Apoli.LOGGER.warn("Ignoring global power set \"{}\" with 'replace' set to true from data pack [{}]. Its loading priority ({}) must be higher than {} to replace the global power set!", id, packName, currLoadingPriority, prevLoadingPriority);
                }

                else {

                    if (globalPowerSet.shouldReplace()) {
                        Apoli.LOGGER.info("Global power set \"{}\" has been replaced by data pack \"{}\"!", id, packName);
                    }

                    List<String> invalidPowers = globalPowerSet.validate()
                        .stream()
                        .map(Power::getId)
                        .map(Identifier::toString)
                        .toList();

                    if (!invalidPowers.isEmpty()) {
                        Apoli.LOGGER.error("Global power set \"{}\" contained {} invalid power(s): {}", id, invalidPowers.size(), String.join(", ", invalidPowers));
                    }

                    loadedGlobalPowerSets.computeIfAbsent(id, k -> new LinkedList<>()).add(entry);
                    DISABLED.remove(id);

                    LOADING_PRIORITIES.put(id, currLoadingPriority);

                }

            }

            catch (Exception e) {
                Apoli.LOGGER.error("There was a problem reading global power set \"{}\" (skipping): {}", id, e.getMessage());
            }

        });

        Apoli.LOGGER.info("Finished reading global power sets from data packs. Merging similar global power sets...");
        loadedGlobalPowerSets.forEach((id, entries) -> {

            AtomicReference<GlobalPowerSet> currentSet = new AtomicReference<>();
            entries.sort(Comparator.comparing(PrioritizedEntry::priority));

            for (PrioritizedEntry<GlobalPowerSet> entry : entries) {

                if (currentSet.get() == null) {
                    currentSet.set(entry.value());
                }

                else {
                    currentSet.accumulateAndGet(entry.value(), (oldSet, newSet) -> merge(dynamicRegistries, oldSet, newSet));
                }

            }

            ALL.put(id, currentSet.get());

        });

        Apoli.LOGGER.info("Finished merging similar global power sets. Registry contains {} global power sets.", ALL.size());

        SerializableData.CURRENT_NAMESPACE = null;
        SerializableData.CURRENT_PATH = null;

        LOADING_PRIORITIES.clear();

    }

    @Override
    public void onReject(String packName, Identifier resourceId) {

        if (!ALL.containsKey(resourceId)) {
            DISABLED.add(resourceId);
        }

    }

    @Override
    public Identifier getFabricId() {
        return Apoli.identifier("global_powers");
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return DEPENDENCIES;
    }

    private static GlobalPowerSet merge(DynamicRegistryManager dynamicRegistries, GlobalPowerSet oldSet, GlobalPowerSet newSet) {

        TagLike.Builder<EntityType<?>> oldBuilder = new TagLike.Builder<>(RegistryKeys.ENTITY_TYPE);
        TagLike.Builder<EntityType<?>> newBuilder = new TagLike.Builder<>(RegistryKeys.ENTITY_TYPE);

        oldSet.getEntityTypes().map(TagLike::entries).ifPresent(oldBuilder::addAll);
        newSet.getEntityTypes().map(TagLike::entries).ifPresent(newBuilder::addAll);

        Set<PowerReference> powerReferences = new ObjectLinkedOpenHashSet<>(oldSet.getPowerReferences());
        int order = oldSet.getOrder();

        if (newSet.shouldReplace()) {

            oldBuilder.clear();
            powerReferences.clear();

            order = newSet.getOrder();

        }

        oldBuilder.addAll(newBuilder);
        powerReferences.addAll(newSet.getPowerReferences());

        Optional<TagLike<EntityType<?>>> entityTypes = oldSet.getEntityTypes().isPresent() || newSet.getEntityTypes().isPresent()
            ? Optional.of(oldBuilder.build(dynamicRegistries.getWrapperOrThrow(RegistryKeys.ENTITY_TYPE)))
            : Optional.empty();

        return new GlobalPowerSet(
            entityTypes,
            powerReferences,
            newSet.shouldReplace(),
            order
        );

    }

}
