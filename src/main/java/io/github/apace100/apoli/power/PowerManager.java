package io.github.apace100.apoli.power;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.integration.*;
import io.github.apace100.apoli.networking.packet.s2c.SyncPowersS2CPacket;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.type.PowerTypes;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.CalioServer;
import io.github.apace100.calio.data.IdentifiableMultiJsonDataLoader;
import io.github.apace100.calio.data.MultiJsonDataContainer;
import io.github.apace100.calio.data.SerializableData;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.*;
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
import net.minecraft.network.codec.PacketCodec;
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
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentProvider;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class PowerManager extends IdentifiableMultiJsonDataLoader implements IdentifiableResourceReloadListener {

    public static final Codec<Identifier> VALIDATING_CODEC = Identifier.CODEC.comapFlatMap(
        id -> contains(id)
            ? DataResult.success(id)
            : DataResult.error(() -> "Couldn't get power from ID \"" + id + "\", as it wasn't registered!"),
        Function.identity()
    );

    public static final Codec<Power> DISPATCH_CODEC = Identifier.CODEC.comapFlatMap(
        PowerManager::getResult,
        Power::getId
    );

    public static final PacketCodec<ByteBuf, Power> DISPATCH_PACKET_CODEC = Identifier.PACKET_CODEC.xmap(
        PowerManager::get,
        Power::getId
    );

    public static final Set<Identifier> DEPENDENCIES = new HashSet<>();

    public static final Identifier ID = Apoli.identifier("powers");
    public static final Identifier PHASE = Apoli.identifier("phase/power_types");

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

    private static final Map<Identifier, Power> POWERS_BY_ID = new HashMap<>();
    private static final Set<Identifier> DISABLED_POWERS = new HashSet<>();

    private static boolean building;
    private static boolean validated;

    public PowerManager() {
        super(GSON, "powers", ResourceType.SERVER_DATA);

        ServerEntityEvents.ENTITY_LOAD.addPhaseOrdering(Event.DEFAULT_PHASE, PHASE);
        ServerEntityEvents.ENTITY_LOAD.register(PHASE, (entity, world) -> {
            if (!(entity instanceof PlayerEntity)) {
                onPostLoad(entity, false);
            }
        });

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(Event.DEFAULT_PHASE, PHASE);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(PHASE, (player, joined) -> {

            send(player);

            //  Sync power holder CCA component upon the player joining the server
            if (joined) {

                List<ServerPlayerEntity> players = player.getServerWorld().getServer().getPlayerManager().getPlayerList();
                players.remove(player);

                players.forEach(otherPlayer -> {
                    PowerHolderComponent.KEY.syncWith(otherPlayer, (ComponentProvider) player);
                    PowerHolderComponent.KEY.syncWith(player, (ComponentProvider) otherPlayer);
                });

            }

            onPostLoad(player, joined);

        });

    }

    @Override
    protected void apply(MultiJsonDataContainer prepared, ResourceManager manager, Profiler profiler) {

        Apoli.LOGGER.info("Reading powers from data packs...");

        PowerReloadCallback.EVENT.invoker().onPowerReload();
        PrePowerReloadCallback.EVENT.invoker().onPrePowerReload();

        startBuilding();
        DynamicRegistryManager dynamicRegistries = CalioServer.getDynamicRegistries().orElse(null);

        if (dynamicRegistries == null) {

            Apoli.LOGGER.error("Can't raed powers from data packs without access to dynamic registries!");
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

        endBuilding();
        Apoli.LOGGER.info("Finished reading powers from data packs. Registry contains {} powers.", size());

        validate();
        PostPowerReloadCallback.EVENT.invoker().onPostPowerReload();

        if (validated) {
            Apoli.LOGGER.info("Finished validating powers from data packs. Registry contains {} powers.", size());
        }

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

    private void onPostLoad(Entity entity, boolean init) {

        RegistryOps<JsonElement> jsonOps = entity.getRegistryManager().getOps(JsonOps.INSTANCE);
        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(entity).orElse(null);

        if (entity.getWorld().isClient || component == null) {
            return;
        }

        boolean mismatch = false;
        for (Power oldPower : component.getPowers(true)) {

            RegistryWrapper.WrapperLookup wrapperLookup = entity.getRegistryManager();
            Identifier powerId = oldPower.getId();

            if (!contains(powerId)) {

                Apoli.LOGGER.error("Removed unregistered power \"{}\" from entity {}!", powerId, entity.getName().getString());
                mismatch = true;

                for (Identifier sourceId : component.getSources(oldPower)) {
                    component.removePower(oldPower, sourceId);
                }

                continue;

            }

            Power newPower = get(powerId);
            PowerType oldPowerType = component.getPowerType(oldPower);

            JsonElement oldPowerJson = Power.CODEC.encodeStart(jsonOps, oldPower).getOrThrow(JsonParseException::new);
            JsonElement newPowerJson = Power.CODEC.encodeStart(jsonOps, newPower).getOrThrow(JsonParseException::new);

            if (oldPowerJson.equals(newPowerJson)) {
                continue;
            }

            Apoli.LOGGER.warn("Mismatched data fields of power \"{}\" from entity {}! Updating...", powerId, entity.getName().getString());
            mismatch = true;

            for (Identifier source : component.getSources(oldPower)) {
                component.removePower(oldPower, source);
                component.addPower(newPower, source);
            }

            PowerType newPowerType = component.getPowerType(newPower);
            if (oldPowerType.getClass().isAssignableFrom(newPowerType.getClass())) {
                //  Transfer the data of the old power to the new power if the old power is an instance of the new power
                Apoli.LOGGER.info("Successfully transferred old data of power \"{}\"!", powerId);
                newPowerType.fromTag(oldPowerType.toTag());
            } else {
                //  Output a warning that the data of the old power couldn't be transferred to the new power. This usually
                //  occurs if the power no longer uses the same power type as it used to
                Apoli.LOGGER.warn("Couldn't transfer old data of power \"{}\", as it's using a different power type!", powerId);
            }

        }

        if (init || mismatch) {

            if (mismatch) {
                Apoli.LOGGER.info("Finished updating power data of entity {}.", entity.getName().getString());
            }

            component.sync();

        }

    }

    private void readMultipleOrNormalPower(DynamicRegistryManager dynamicRegistries, String packName, Identifier powerId, JsonObject powerJson) {

        powerJson.addProperty("id", powerId.toString());

        PrePowerLoadCallback.EVENT.invoker().onPrePowerLoad(powerId, powerJson);
        Power basePower = Power.CODEC.parse(dynamicRegistries.getOps(JsonOps.INSTANCE), powerJson).getOrThrow(JsonParseException::new);

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

                        if (this.readSubPower(dynamicRegistries, packName, powerId, subPowerId, key, subPowerJson)) {
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

    private boolean readSubPower(DynamicRegistryManager dynamicRegistries, String packName, Identifier superPowerId, Identifier subPowerId, String name, JsonObject subPowerJson) {

        if (!ResourceConditionsImpl.applyResourceConditions(subPowerJson, directoryName, subPowerId, dynamicRegistries)) {
            this.onReject(packName, subPowerId);
            return false;
        }

        else {

            subPowerJson.addProperty("id", subPowerId.toString());
            Power basePower = Power.CODEC.parse(dynamicRegistries.getOps(JsonOps.INSTANCE), subPowerJson).getOrThrow(JsonParseException::new);

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

        int priority = JsonHelper.getInt(powerJson, "loading_priority", 0);
        int previousPriority = LOADING_PRIORITIES.computeIfAbsent(powerId, k -> priority);

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

        if (!building) {
            return power;
        }

        else if (POWERS_BY_ID.containsKey(id)) {
            throw new IllegalArgumentException("Tried to register duplicate power with ID \"" + id + "\"");
        }

        else {

            DISABLED_POWERS.remove(id);
            POWERS_BY_ID.put(id, power);

            return power;

        }

    }

    private static Power update(Identifier id, Power power) {

        if (!building) {
            return power;
        }

        else {

            if (remove(id) instanceof MultiplePower removedMultiplePower) {
                removedMultiplePower.getSubPowers()
                    .stream()
                    .map(Power::getId)
                    .forEach(PowerManager::remove);
            }

            PowerOverrideCallback.EVENT.invoker().onPowerOverride(id);
            return register(id, power);

        }

    }

    private static Power remove(Identifier id) {

        if (building) {
            return POWERS_BY_ID.remove(id);
        }

        else {
            return null;
        }

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
        validated = true;

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

    }

    private static void startBuilding() {

        if (building) {
            return;
        }

        PowerClearCallback.EVENT.invoker().onPowerClear();

        building = true;
        validated = false;

        POWERS_BY_ID.clear();
        DISABLED_POWERS.clear();

        LOADING_PRIORITIES.clear();

    }

    private static void endBuilding() {
        building = false;
    }

    public void send(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new SyncPowersS2CPacket(POWERS_BY_ID));
    }

    @Environment(EnvType.CLIENT)
    public static void receive(SyncPowersS2CPacket packet, ClientPlayNetworking.Context context) {
        startBuilding();
        packet.powersById().forEach(PowerManager::register);
        endBuilding();
    }

    public static DataResult<Power> getResult(Identifier id) {

        try {
            return DataResult.success(get(id));
        }

        catch (Exception e) {
            return DataResult.error(e::getMessage);
        }

    }

    public static Optional<Power> getOptional(Identifier id) {
        return Optional.ofNullable(POWERS_BY_ID.get(id));
    }

    public static Power get(Identifier id) {
        return getOptional(id).orElseThrow(() -> new IllegalArgumentException("Couldn't get power from ID \"" + id + "\", as it wasn't registered!"));
    }

    public static Set<Map.Entry<Identifier, Power>> entrySet() {
        return new ObjectArraySet<>(POWERS_BY_ID.entrySet());
    }

    public static Collection<Power> values() {
        return new ObjectArrayList<>(POWERS_BY_ID.values());
    }

    public static Stream<Identifier> streamIds() {
        return new ObjectArraySet<>(POWERS_BY_ID.keySet()).stream();
    }

    public static void forEach(BiConsumer<Identifier, Power> biConsumer) {
        POWERS_BY_ID.forEach(biConsumer);
    }

    public static boolean isDisabled(Identifier id) {
        return DISABLED_POWERS.contains(id);
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
            || Power.DATA.containsField(field);
    }

}
