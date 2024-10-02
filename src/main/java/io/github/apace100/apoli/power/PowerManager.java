package io.github.apace100.apoli.power;

import com.google.gson.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.integration.*;
import io.github.apace100.apoli.networking.packet.s2c.SyncPowersS2CPacket;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.power.type.PowerTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.CalioServer;
import io.github.apace100.calio.data.IdentifiableMultiJsonDataLoader;
import io.github.apace100.calio.data.MultiJsonDataContainer;
import io.github.apace100.calio.data.SerializableData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public class PowerManager extends IdentifiableMultiJsonDataLoader implements IdentifiableResourceReloadListener {

    public static final Set<Identifier> DEPENDENCIES = new HashSet<>();
    public static final Identifier ID = Apoli.identifier("powers");

    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();

    private static final Set<String> FIELDS_TO_IGNORE = Set.of(
        "condition",
        "loading_priority",
        ResourceConditions.CONDITIONS_KEY
    );

    private static final Map<Identifier, Integer> LOADING_PRIORITIES = new HashMap<>();
    private static final Map<String, AdditionalPowerDataCallback> ADDITIONAL_DATA = new HashMap<>();

    private static final Object2ObjectOpenHashMap<Identifier, Power> POWERS_BY_ID = new Object2ObjectOpenHashMap<>();
    private static final ObjectOpenHashSet<Identifier> DISABLED_POWERS = new ObjectOpenHashSet<>();

    public PowerManager() {
        super(GSON, "powers", ResourceType.SERVER_DATA);

        ServerEntityEvents.ENTITY_LOAD.addPhaseOrdering(Event.DEFAULT_PHASE, ID);
        ServerEntityEvents.ENTITY_LOAD.register(ID, (entity, world) -> {
            if (!(entity instanceof PlayerEntity)) {
                updateData(entity, false);
            }
        });

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(Event.DEFAULT_PHASE, ID);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(ID, (player, joined) -> {
            send(player);
            updateData(player, joined);
        });

    }

    @Override
    protected void apply(MultiJsonDataContainer prepared, ResourceManager manager, Profiler profiler) {

        Apoli.LOGGER.info("Reading powers from data packs...");

        PowerReloadCallback.EVENT.invoker().onPowerReload();
        PrePowerReloadCallback.EVENT.invoker().onPrePowerReload();

        DynamicRegistryManager dynamicRegistries = CalioServer.getDynamicRegistries().orElse(null);
        startBuilding();

        if (dynamicRegistries == null) {

            Apoli.LOGGER.error("Can't read powers from data packs without access to dynamic registries!");
            endBuilding();

            return;

        }

        prepared.forEach((packName, id, jsonElement) -> {

            try {

                SerializableData.CURRENT_NAMESPACE = id.getNamespace();
                SerializableData.CURRENT_PATH = id.getPath();

                if (jsonElement instanceof JsonObject jsonObject) {
                    this.readMultipleOrNormalPower(dynamicRegistries, packName, id, jsonObject);
                }

                else {
                    throw new JsonSyntaxException("Not a JSON object: " + jsonElement);
                }

            }

            catch (Exception e) {
                Apoli.LOGGER.error("There was a problem reading power \"{}\" from data pack [{}]: {}", id, packName, e.getMessage());
            }

        });

        SerializableData.CURRENT_NAMESPACE = null;
        SerializableData.CURRENT_PATH = null;

        Apoli.LOGGER.info("Finished reading powers from data packs. Registry contains {} powers.", size());

        validate();
        endBuilding();

    }

    @Override
    public void onReject(String packName, Identifier resourceId) {

        if (!contains(resourceId)) {
            disable(resourceId);
        }

    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return DEPENDENCIES;
    }

    private void updateData(Entity entity, boolean initialize) {

        RegistryOps<JsonElement> jsonOps = entity.getRegistryManager().getOps(JsonOps.INSTANCE);
        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(entity);

        if (component == null) {
            return;
        }

        int mismatches = 0;

        for (Power oldPower : component.getPowers(true)) {

            StringBuilder oldPowerString = new StringBuilder();
            if (oldPower instanceof SubPower subPower) {
                oldPowerString.append("sub-power \"")
                    .append(subPower.getSubName())
                    .append("\" of power \"")
                    .append(subPower.getSuperPowerId())
                    .append("\"");
            }

            else {
                oldPowerString.append("power \"")
                    .append(oldPower.getId())
                    .append("\"");
            }

            if (!contains(oldPower)) {

                Apoli.LOGGER.error("Removed unregistered {} from entity {}!", oldPowerString, entity.getName().getString());

                for (Identifier sourceId : component.getSources(oldPower)) {
                    component.removePower(oldPower, sourceId);
                }

            }

            else {

                Power newPower = get(oldPower.getId());
                PowerType oldPowerType = component.getPowerType(oldPower);

                JsonElement oldPowerJson = Power.DATA_TYPE.write(jsonOps, oldPower).getOrThrow(JsonParseException::new);
                JsonElement newPowerJson = Power.DATA_TYPE.write(jsonOps, newPower).getOrThrow(JsonParseException::new);

                if (oldPowerJson.equals(newPowerJson)) {
                    continue;
                }

                Apoli.LOGGER.warn("{} from entity {} has mismatched data fields! Updating...", StringUtils.capitalize(oldPowerString.toString()), entity.getName().getString());
                mismatches++;

                for (Identifier source : component.getSources(oldPower)) {
                    component.removePower(oldPower, source);
                    component.addPower(newPower, source);
                }

                PowerType newPowerType = component.getPowerType(newPower);
                if (oldPowerType.getClass().isAssignableFrom(newPowerType.getClass())) {
                    //  Transfer the data of the old power to the new power if the old power is an instance of the new power
                    Apoli.LOGGER.info("Successfully transferred old data of {}!", oldPowerString);
                    newPowerType.fromTag(oldPowerType.toTag());
                }

                else {
                    //  Output a warning that the data of the old power couldn't be transferred to the new power. This usually
                    //  occurs if the power no longer uses the same power type as it used to
                    Apoli.LOGGER.warn("Couldn't transfer old data of {}, as it's using a different power type!", oldPowerString);
                }

            }

        }

        if (mismatches > 0) {
            Apoli.LOGGER.info("Finished updating {} powers with mismatched data fields from entity {}!", mismatches, entity.getName().getString());
        }

        component.sync();

    }

    private void readMultipleOrNormalPower(RegistryWrapper.WrapperLookup wrapperLookup, String packName, Identifier powerId, JsonObject powerJson) {

        powerJson.addProperty("id", powerId.toString());

        PrePowerLoadCallback.EVENT.invoker().onPrePowerLoad(powerId, powerJson);
        Power basePower = Power.DATA_TYPE.read(wrapperLookup.getOps(JsonOps.INSTANCE), powerJson).getOrThrow(JsonParseException::new);

        if (basePower.isMultiple()) {

            Power supposedMultiplePower = this.readPower(packName, new MultiplePower(basePower), powerJson);
            Set<Identifier> subPowerIds = new ObjectLinkedOpenHashSet<>();

            powerJson.asMap().forEach((key, jsonElement) -> {

                if (shouldIgnoreField(key)) {
                    return;
                }

                try {

                    if (!Identifier.isPathValid(key)) {
                        throw new InvalidIdentifierException("Non [a-z0-9/._-] character in sub-power name \"" + key + "\"!");
                    }

                    else if (jsonElement instanceof JsonObject subPowerJson) {

                        Identifier subPowerId = powerId.withSuffixedPath("_" + key);

                        if (this.readSubPower(wrapperLookup, packName, powerId, subPowerId, key, subPowerJson)) {
                            subPowerIds.add(subPowerId);
                        }

                    }

                    else {
                        throw new JsonSyntaxException("Not a JSON object: " + jsonElement);
                    }

                }

                catch (Exception e) {
                    Apoli.LOGGER.error("There was a problem reading sub-power \"{}\" in power \"{}\" from data pack [{}]: {}", key, powerId, packName, e.getMessage());
                }

            });

            if (supposedMultiplePower instanceof MultiplePower multiplePower) {
                multiplePower.setSubPowerIds(subPowerIds);
            }

            else if (isDisabled(powerId)) {
                subPowerIds.forEach(PowerManager::disable);
            }

        }

        else {
            this.readPower(packName, basePower, powerJson);
        }

    }

    private boolean readSubPower(RegistryWrapper.WrapperLookup wrapperLookup, String packName, Identifier superPowerId, Identifier subPowerId, String name, JsonObject subPowerJson) {

        if (!ResourceConditionsImpl.applyResourceConditions(subPowerJson, directoryName, subPowerId, wrapperLookup)) {
            this.onReject(packName, subPowerId);
            return false;
        }

        else {

            subPowerJson.addProperty("id", subPowerId.toString());
            Power basePower = Power.DATA_TYPE.read(wrapperLookup.getOps(JsonOps.INSTANCE), subPowerJson).getOrThrow(JsonParseException::new);

            SubPower subPower = switch (this.readPower(packName, new SubPower(superPowerId, name, basePower), subPowerJson)) {
                case SubPower selfSubPower ->
                    selfSubPower;
                case Power power ->
                    new SubPower(superPowerId, name, power);
                case null ->
                    null;
            };

            if (subPower != null && subPower.isMultiple()) {
                throw new IllegalStateException("Using the '" + PowerTypes.MULTIPLE.getSerializerId() + "' power type in sub-powers is not allowed!");
            }

            else {
                return subPower != null;
            }

        }

    }

    @Nullable
    private <P extends Power> Power readPower(String packName, P power, JsonObject powerJson) {

        Identifier powerId = power.getId();

        int previousPriority = LOADING_PRIORITIES.getOrDefault(powerId, 0);
        int priority = JsonHelper.getInt(powerJson, "loading_priority", 0);

        if (!contains(powerId)) {
            return this.finishReadingPower(PowerManager::register, powerId, power, powerJson, priority);
        }

        else if (previousPriority < priority) {

            StringBuilder overrideMessage = new StringBuilder("Overriding ");
            if (power instanceof SubPower subPower) {
                overrideMessage
                    .append("sub-power \"")
                    .append(subPower.getSubName())
                    .append("\" of power \"")
                    .append(subPower.getSuperPowerId())
                    .append("\"");
            }

            else {
                overrideMessage.append("power \"")
                    .append(power.getId())
                    .append("\"");
            }

            Apoli.LOGGER.warn(overrideMessage
                .append(" (with a previous loading priority of ")
                .append(previousPriority)
                .append(") with the same power that has a higher loading priority of ")
                .append(priority)
                .append(" from data pack [")
                .append(packName)
                .append("]!"));

            return this.finishReadingPower(PowerManager::update, powerId, power, powerJson, priority);

        }

        else {

            StringBuilder overrideHint = new StringBuilder("Ignoring ");
            if (power instanceof SubPower subPower) {
                overrideHint
                    .append("sub-power \"")
                    .append(subPower.getSubName())
                    .append("\" of power \"")
                    .append(subPower.getSuperPowerId())
                    .append("\"");
            }

            else {
                overrideHint.append("power \"")
                    .append(power.getId())
                    .append("\"");
            }

            Apoli.LOGGER.warn(overrideHint
                .append(" from data pack [")
                .append(packName)
                .append("]. Its loading priority must be higher than ")
                .append(previousPriority)
                .append(" in order to override the same power added by a previous data pack!"));

            return power.isSubPower()
                ? get(powerId)
                : null;

        }

    }

    private <P extends Power> P finishReadingPower(BiFunction<Identifier, Power, Power> powerTypeProcessor, Identifier powerId, P power, JsonObject jsonObject, int priority) {

        Identifier powerFactoryId = power.getFactoryInstance().getFactory().getSerializerId();
        boolean subPower = power.isSubPower();

        powerTypeProcessor.apply(powerId, power);
        LOADING_PRIORITIES.put(powerId, priority);

        handleAdditionalData(powerId, powerFactoryId, subPower, jsonObject, power);
        PostPowerLoadCallback.EVENT.invoker().onPostPowerLoad(powerId, powerFactoryId, subPower, jsonObject, power);

        return power;

    }

    private static Power register(Identifier id, Power power) {

        if (contains(id)) {
            throw new IllegalArgumentException("Tried to register duplicate power with ID \"" + id + "\"");
        }

        else {

            DISABLED_POWERS.remove(id);
            POWERS_BY_ID.put(id, power);

            return power;

        }

    }

    private static Power update(Identifier id, Power power) {

        if (remove(id) instanceof MultiplePower removedMultiplePower) {
            removedMultiplePower.getSubPowers()
                .stream()
                .map(Power::getId)
                .forEach(PowerManager::remove);
        }

        PowerOverrideCallback.EVENT.invoker().onPowerOverride(id);
        return register(id, power);

    }

    private static Power remove(Identifier id) {
        return POWERS_BY_ID.remove(id);
    }

    public static void disable(Identifier id) {
        remove(id);
        DISABLED_POWERS.add(id);
    }

    /**
     *  Validates all registered powers.
     */
    public static void validate() {

        if (POWERS_BY_ID.isEmpty()) {
            return;
        }

        Apoli.LOGGER.info("Validating {} powers...", size());
        Iterator<Map.Entry<Identifier, Power>> powerTypeIterator = POWERS_BY_ID.entrySet().iterator();

        while (powerTypeIterator.hasNext()) {

            Map.Entry<Identifier, Power> powerTypeEntry = powerTypeIterator.next();

            Identifier id = powerTypeEntry.getKey();
            Power power = powerTypeEntry.getValue();

            try {
                power.validate();
            }

            catch (Exception e) {

                StringBuilder errorBuilder = new StringBuilder("There was a problem validating ");
                powerTypeIterator.remove();

                if (power instanceof SubPower subPowerType) {
                    errorBuilder
                        .append("sub-power \"")
                        .append(subPowerType.getSubName())
                        .append("\" in power \"")
                        .append(subPowerType.getSuperPowerId())
                        .append("\"");
                }

                else {
                    errorBuilder
                        .append("power \"")
                        .append(id)
                        .append("\"");
                }

                Apoli.LOGGER.error(errorBuilder
                    .append(" (removing): ")
                    .append(e.getMessage()));

            }

        }

        Apoli.LOGGER.info("Finished validating powers from data packs. Registry contains {} powers.", size());

    }

    private static void startBuilding() {

        LOADING_PRIORITIES.clear();

        POWERS_BY_ID.clear();
        DISABLED_POWERS.clear();

        PowerClearCallback.EVENT.invoker().onPowerClear();

    }

    private static void endBuilding() {

        LOADING_PRIORITIES.clear();

        POWERS_BY_ID.trim();
        DISABLED_POWERS.trim();

        PostPowerReloadCallback.EVENT.invoker().onPostPowerReload();

    }

    public void send(ServerPlayerEntity player) {

        if (player.server.isDedicated()) {
            ServerPlayNetworking.send(player, new SyncPowersS2CPacket(POWERS_BY_ID));
        }

    }

    @Environment(EnvType.CLIENT)
    public static void receive(SyncPowersS2CPacket packet, ClientPlayNetworking.Context context) {
        startBuilding();
        packet.powersById().forEach(PowerManager::register);
        endBuilding();
    }

    public static DataResult<Power> getResult(Identifier id) {
        return contains(id)
            ? DataResult.success(POWERS_BY_ID.get(id))
            : DataResult.error(() -> "Couldn't get power from ID \"" + id + "\", as it wasn't registered!");
    }

    public static Optional<Power> getOptional(Identifier id) {
        return getResult(id).result();
    }

    @Nullable
    public static Power getNullable(Identifier id) {
        return POWERS_BY_ID.get(id);
    }

    public static Power get(Identifier id) {
        return getResult(id).getOrThrow();
    }

    public static Set<Map.Entry<Identifier, Power>> entrySet() {
        return new ObjectOpenHashSet<>(POWERS_BY_ID.entrySet());
    }

    public static Set<Identifier> keySet() {
        return new ObjectOpenHashSet<>(POWERS_BY_ID.keySet());
    }

    public static Collection<Power> values() {
        return new ObjectOpenHashSet<>(POWERS_BY_ID.values());
    }

    public static boolean isDisabled(Identifier id) {
        return DISABLED_POWERS.contains(id);
    }

    public static boolean contains(Power power) {
        return contains(power.getId());
    }

    public static boolean contains(Identifier id) {
        return POWERS_BY_ID.containsKey(id);
    }

    public static int size() {
        return POWERS_BY_ID.size();
    }

    private static void handleAdditionalData(Identifier powerId, Identifier factoryId, boolean isSubPower, JsonObject json, Power power) {
        ADDITIONAL_DATA.entrySet()
            .stream()
            .filter(entry -> json.has(entry.getKey()))
            .forEach(entry -> entry.getValue().readAdditionalPowerData(powerId, factoryId, isSubPower, json.get(entry.getKey()), power));
    }

    public static void registerAdditionalData(String field, AdditionalPowerDataCallback callback) {

        if (ADDITIONAL_DATA.containsKey(field)) {
            Apoli.LOGGER.error("Cannot add additional data callback for field \"{}\", as Apoli already contains a callback for it!", field);
            return;
        }

        for (PowerTypeFactory<?> powerTypeFactory : ApoliRegistries.POWER_FACTORY) {

            if (powerTypeFactory.getSerializableData().containsField(field)) {
                Apoli.LOGGER.error("Cannot add additional data callback for field \"{}\" as it's already used by the \"{}\" power type!", field, powerTypeFactory.getSerializerId());
                return;
            }

        }

        ADDITIONAL_DATA.put(field, callback);

    }

    public static boolean shouldIgnoreField(String field) {
        return field.isEmpty()
            || field.startsWith("$")
            || FIELDS_TO_IGNORE.contains(field)
            || ADDITIONAL_DATA.containsKey(field)
            || Power.DATA_TYPE.serializableData().containsField(field);
    }

}
