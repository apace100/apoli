package io.github.apace100.apoli.power;

import com.google.gson.*;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.integration.*;
import io.github.apace100.apoli.networking.packet.s2c.SyncPowerTypeRegistryS2CPacket;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.ApoliResourceConditions;
import io.github.apace100.apoli.util.IdentifierAlias;
import io.github.apace100.calio.data.IdentifiableMultiJsonDataLoader;
import io.github.apace100.calio.data.MultiJsonDataContainer;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

@SuppressWarnings("rawtypes")
public class PowerTypes extends IdentifiableMultiJsonDataLoader implements IdentifiableResourceReloadListener {

    public static final Set<Identifier> DEPENDENCIES = new HashSet<>();
    public static final Set<String> LOADED_NAMESPACES = new HashSet<>();

    public static final Identifier PHASE = Apoli.identifier("phase/power_types");
    private static final Identifier MULTIPLE = Apoli.identifier("multiple");
    private static final Identifier SIMPLE = Apoli.identifier("simple");

    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();

    private static final Map<Identifier, Integer> LOADING_PRIORITIES = new HashMap<>();
    private static final Map<String, AdditionalPowerDataCallback> ADDITIONAL_DATA = new HashMap<>();

    private static final Set<String> FIELDS_TO_IGNORE = Set.of(
        "type",
        "loading_priority",
        "name",
        "description",
        "hidden",
        "condition",
        ResourceConditions.CONDITIONS_KEY
    );

    public PowerTypes() {
        super(GSON, "powers", ResourceType.SERVER_DATA);

        ServerEntityEvents.ENTITY_LOAD.addPhaseOrdering(Event.DEFAULT_PHASE, PHASE);
        ServerEntityEvents.ENTITY_LOAD.register(PHASE, (entity, world) -> {
            if (!(entity instanceof PlayerEntity)) {
                postLoad(entity, false);
            }
        });

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(Event.DEFAULT_PHASE, PHASE);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(PHASE, (player, joined) -> {

            Map<Identifier, PowerType<?>> powers = new HashMap<>();
            PowerTypeRegistry.forEach(powers::put);

            ServerPlayNetworking.send(player, new SyncPowerTypeRegistryS2CPacket(powers));

            //  Sync power holder CCA component upon the player joining the server
            if (joined) {

                List<ServerPlayerEntity> players = player.getServerWorld().getServer().getPlayerManager().getPlayerList();
                players.remove(player);

                players.forEach(otherPlayer -> {
                    PowerHolderComponent.KEY.syncWith(otherPlayer, (ComponentProvider) player);
                    PowerHolderComponent.KEY.syncWith(player, (ComponentProvider) otherPlayer);
                });

            }

            postLoad(player, joined);

        });

    }

    private void postLoad(Entity entity, boolean init) {

        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(entity).orElse(null);
        if (entity.getWorld().isClient || component == null) {
            return;
        }

        boolean mismatch = false;
        for (PowerType<?> oldPowerType : component.getPowerTypes(true)) {

            Identifier oldPowerTypeId = oldPowerType.getIdentifier();
            if (!PowerTypeRegistry.contains(oldPowerTypeId)) {

                Apoli.LOGGER.error("Removed unregistered power \"{}\" from entity {}!", oldPowerTypeId, entity.getName().getString());
                mismatch = true;

                for (Identifier sourceId : component.getSources(oldPowerType)) {
                    component.removePower(oldPowerType, sourceId);
                }

                continue;

            }

            PowerType<?> newPowerType = PowerTypeRegistry.get(oldPowerTypeId);
            Power oldPower = component.getPower(oldPowerType);

            if (oldPowerType.toJson().equals(newPowerType.toJson())) {
                continue;
            }

            Apoli.LOGGER.warn("Mismatched data fields of power \"{}\" from entity {}! Updating...", oldPowerTypeId, entity.getName().getString());
            mismatch = true;

            for (Identifier source : component.getSources(oldPowerType)) {
                component.removePower(oldPowerType, source);
                component.addPower(newPowerType, source);
            }

            Power newPower = component.getPower(newPowerType);
            if (oldPower.getClass().isAssignableFrom(newPower.getClass())) {
                //  Transfer the data of the old power to the new power if the old power is an instance of the new power
                Apoli.LOGGER.info("Successfully transferred old data of power \"{}\"!", oldPowerTypeId);
                newPower.fromTag(oldPower.toTag());
            } else {
                //  Output a warning that the data of the old power couldn't be transferred to the new power. This usually
                //  occurs if the power no longer uses the same power type as it used to
                Apoli.LOGGER.warn("Couldn't transfer old data of power \"{}\", as it's using a different power type!", oldPowerTypeId);
            }

        }

        if (init || mismatch) {

            if (mismatch) {
                Apoli.LOGGER.info("Finished updating power data of entity {}.", entity.getName().getString());
            }

            component.sync();

        }

    }

    @Override
    protected void apply(MultiJsonDataContainer prepared, ResourceManager manager, Profiler profiler) {

        PowerTypeRegistry.reset();
        LOADING_PRIORITIES.clear();

        LOADED_NAMESPACES.clear();
        LOADED_NAMESPACES.addAll(manager.getAllNamespaces());

        PowerReloadCallback.EVENT.invoker().onPowerReload();
        PrePowerReloadCallback.EVENT.invoker().onPrePowerReload();

        prepared.forEach((packName, id, jsonElement) -> {
            try {

                SerializableData.CURRENT_NAMESPACE = id.getNamespace();
                SerializableData.CURRENT_PATH = id.getPath();

                JsonObject jsonObject = jsonElement.getAsJsonObject();
                PrePowerLoadCallback.EVENT.invoker().onPrePowerLoad(id, jsonObject);

                Identifier powerFactoryId = new Identifier(JsonHelper.getString(jsonObject, "type"));

                if (!isMultiple(powerFactoryId)) {
                    readPower(packName, id, jsonObject);
                    return;
                }

                List<Identifier> subPowerIds = new LinkedList<>();
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {

                    String field = entry.getKey();
                    if (shouldIgnoreField(field)) {
                        continue;
                    }

                    Identifier subPowerId = new Identifier(id + "_" + field);
                    try {
                        PowerType<?> subPower = readSubPower(packName, subPowerId, entry.getValue().getAsJsonObject());
                        if (subPower != null) {
                            subPowerIds.add(subPowerId);
                        }
                    } catch (Exception e) {
                        Apoli.LOGGER.error("There was a problem reading sub-power \"{}\" in power file \"{}\" (skipping): {}", subPowerId, id, e.getMessage());
                    }

                }

                MultiplePowerType<?> superPower = readSuperPower(packName, id, jsonObject);
                if (superPower == null) {
                    subPowerIds.forEach(PowerTypeRegistry::disable);
                    return;
                }

                superPower.setSubPowers(subPowerIds);
                handleAdditionalData(id, powerFactoryId, false, jsonObject, superPower);
                PostPowerLoadCallback.EVENT.invoker().onPostPowerLoad(id, powerFactoryId, false, jsonObject, superPower);

            } catch (Exception e) {
                Apoli.LOGGER.error("There was a problem reading power file \"{}\" (skipping): {}", id, e.getMessage());
            }
        });

        PostPowerReloadCallback.EVENT.invoker().onPostPowerReload();
        LOADING_PRIORITIES.clear();

        SerializableData.CURRENT_NAMESPACE = null;
        SerializableData.CURRENT_PATH = null;

        Apoli.LOGGER.info("Finished loading powers from data files. Registry contains {} powers.", PowerTypeRegistry.size());

    }

    private boolean isResourceConditionValid(Identifier id, JsonObject jo) {
        return ApoliResourceConditions.test(id, jo);
    }

    private void readPower(String packName, Identifier id, JsonObject jsonObject) {
        readPower(packName, id, jsonObject, false, PowerType::new);
    }

    @Nullable
    private MultiplePowerType<?> readSuperPower(String packName, Identifier id, JsonObject jsonObject) {
        PowerType<?> powerType = readPower(packName, id, jsonObject, false, MultiplePowerType::new);
        return powerType instanceof MultiplePowerType<?> multiplePowerType ? multiplePowerType : null;
    }

    @Nullable
    private PowerType<?> readSubPower(String packName, Identifier id, JsonObject jsonObject) {
        return readPower(packName, id, jsonObject, true, PowerType::new);
    }

    @Nullable
    private PowerType<?> readPower(String packName, Identifier id, JsonObject jsonObject, boolean isSubPower, BiFunction<Identifier, PowerFactory<?>.Instance, PowerType<?>> powerTypeFactory) {

        Identifier powerFactoryId = new Identifier(JsonHelper.getString(jsonObject, "type"));
        int loadingPriority = JsonHelper.getInt(jsonObject, "loading_priority", 0);

        if (!isResourceConditionValid(id, jsonObject)) {
            if (!PowerTypeRegistry.contains(id)) {
                PowerTypeRegistry.disable(id);
            }
            return null;
        }

        if (isMultiple(powerFactoryId)) {
            powerFactoryId = SIMPLE;
            if (isSubPower) {
                throw new JsonSyntaxException("Power type \"" + MULTIPLE + "\" cannot be used in a sub-power that uses the \"" + MULTIPLE + "\" power type.");
            }
        }

        int prevLoadingPriority = getLoadingPriority(id);
        if (!PowerTypeRegistry.contains(id)) {
            return finishReadingPower(PowerTypeRegistry::register, powerTypeFactory, id, powerFactoryId, jsonObject, isSubPower, loadingPriority);
        } else if (prevLoadingPriority < loadingPriority) {
            Apoli.LOGGER.warn("Overriding power \"{}\" (with prev. loading priority of {}) with a higher loading priority of {} from data pack [{}]!", id, prevLoadingPriority, loadingPriority, packName);
            return finishReadingPower(PowerTypeRegistry::update, powerTypeFactory, id, powerFactoryId, jsonObject, isSubPower, loadingPriority);
        }

        return null;

    }

    private PowerType<?> finishReadingPower(BiFunction<Identifier, PowerType<?>, PowerType<?>> powerTypeProcessor, BiFunction<Identifier, PowerFactory<?>.Instance, PowerType<?>> powerTypeFactory, Identifier powerId, Identifier powerFactoryId, JsonObject jsonObject, boolean isSubPower, int priority) {

        Optional<PowerFactory> powerFactory = ApoliRegistries.POWER_FACTORY.getOrEmpty(powerFactoryId);
        if (powerFactory.isEmpty() && IdentifierAlias.hasAlias(powerFactoryId)) {
            powerFactory = ApoliRegistries.POWER_FACTORY.getOrEmpty(IdentifierAlias.resolveAlias(powerFactoryId));
        }

        PowerFactory<?>.Instance powerFactoryInstance = powerFactory
            .orElseThrow(() -> new JsonSyntaxException("Power type \"" + powerFactoryId + "\" is not registered."))
            .read(jsonObject);

        PowerType<?> powerType = loadPower(powerId, powerFactoryInstance, powerTypeFactory, jsonObject, isSubPower);
        powerTypeProcessor.apply(powerId, powerType);

        LOADING_PRIORITIES.put(powerId, priority);
        if (!(powerType instanceof MultiplePowerType<?>)) {
            handleAdditionalData(powerId, powerFactoryId, isSubPower, jsonObject, powerType);
            PostPowerLoadCallback.EVENT.invoker().onPostPowerLoad(powerId, powerFactoryId, isSubPower, jsonObject, powerType);
        }

        return powerType;

    }

    private PowerType<?> loadPower(Identifier id, PowerFactory<?>.Instance powerFactoryInstance, BiFunction<Identifier, PowerFactory<?>.Instance, PowerType<?>> powerTypeFactory, JsonObject jsonObject, boolean isSubPower) {

        JsonElement nameJson = jsonObject.get("name");
        JsonElement descriptionJson = jsonObject.get("description");

        Text name = nameJson == null ? null : Text.Serializer.fromJson(nameJson);
        Text description = descriptionJson == null ? null : Text.Serializer.fromJson(descriptionJson);

        boolean hidden = JsonHelper.getBoolean(jsonObject, "hidden", false);

        PowerType<?> powerType = powerTypeFactory.apply(id, powerFactoryInstance);
        powerType.setDisplayTexts(name, description);

        if (hidden || isSubPower) {
            powerType.setHidden();
        }

        return powerType;

    }

    private boolean shouldIgnoreField(String field) {
        return field.startsWith("$")
            || FIELDS_TO_IGNORE.contains(field)
            || ADDITIONAL_DATA.containsKey(field);
    }

    private boolean isMultiple(Identifier id) {
        return MULTIPLE.equals(id)
            || (IdentifierAlias.hasAlias(id) && MULTIPLE.equals(IdentifierAlias.resolveAlias(id)));
    }

    private void handleAdditionalData(Identifier powerId, Identifier factoryId, boolean isSubPower, JsonObject json, PowerType<?> powerType) {
        ADDITIONAL_DATA.forEach((dataFieldName, callback) -> {
            if(json.has(dataFieldName)) {
                callback.readAdditionalPowerData(powerId, factoryId, isSubPower, json.get(dataFieldName), powerType);
            }
        });
    }

    @Override
    public Identifier getFabricId() {
        return Apoli.identifier("powers");
    }

    @SuppressWarnings("unused")
    public static void registerAdditionalData(String data, AdditionalPowerDataCallback callback) {
        if(ADDITIONAL_DATA.containsKey(data)) {
            Apoli.LOGGER.error("Apoli already contains a callback for additional data for the field \"" + data + "\".");
            return;
        }
        // TODO: Check whether any power factory uses that data field?
        ADDITIONAL_DATA.put(data, callback);
    }

    public static int getLoadingPriority(Identifier powerId) {
        return LOADING_PRIORITIES.getOrDefault(powerId, 0);
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return DEPENDENCIES;
    }
}