package io.github.apace100.apoli.power;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.integration.*;
import io.github.apace100.apoli.networking.packet.s2c.SyncPowerTypesS2CPacket;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.SubPowerException;
import io.github.apace100.calio.Calio;
import io.github.apace100.calio.data.IdentifiableMultiJsonDataLoader;
import io.github.apace100.calio.data.MultiJsonDataContainer;
import io.github.apace100.calio.data.SerializableData;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
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
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
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

    private static ImmutableMap<Identifier, Power> powersById = ImmutableMap.of();
    private static Map<Identifier, Power> powersByIdBuilder;

    private static ImmutableSet<Identifier> disabledPowers = ImmutableSet.of();
    private static Set<Identifier> disabledPowersBuilder;

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

        LOADING_PRIORITIES.clear();
        startBuild();

        PowerReloadCallback.EVENT.invoker().onPowerReload();
        PrePowerReloadCallback.EVENT.invoker().onPrePowerReload();

        prepared.forEach((packName, id, jsonElement) -> {

            try {

                SerializableData.CURRENT_NAMESPACE = id.getNamespace();
                SerializableData.CURRENT_PATH = id.getPath();

                if (!(jsonElement instanceof JsonObject jsonObject)) {
                    throw new JsonSyntaxException("Expected a JSON object");
                }

                PrePowerLoadCallback.EVENT.invoker().onPrePowerLoad(id, jsonObject);
                this.readSuperOrNormalPower(packName, id, jsonObject);

            }

            catch (SubPowerException spe) {
                Apoli.LOGGER.error(spe.getMessage());
            }

            catch (Exception e) {
                Apoli.LOGGER.error("There was a problem reading power \"{}\": {}", id, e.getMessage());
            }

        });

        LOADING_PRIORITIES.clear();

        SerializableData.CURRENT_NAMESPACE = null;
        SerializableData.CURRENT_PATH = null;

        Apoli.LOGGER.info("Finished loading powers from data files. Registry contains {} powers", size());
        validate();

        PostPowerReloadCallback.EVENT.invoker().onPostPowerReload();
        endBuild();

        if (validated) {
            Apoli.LOGGER.info("Finished validating powers from data files. Registry contains {} powers.", size());
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

        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(entity).orElse(null);
        if (entity.getWorld().isClient || component == null) {
            return;
        }

        boolean mismatch = false;
        for (Power oldPower : component.getPowers(true)) {

            Identifier oldPowerId = oldPower.getId();
            if (!contains(oldPowerId)) {

                Apoli.LOGGER.error("Removed unregistered power \"{}\" from entity {}!", oldPowerId, entity.getName().getString());
                mismatch = true;

                for (Identifier sourceId : component.getSources(oldPower)) {
                    component.removePower(oldPower, sourceId);
                }

                continue;

            }

            Power newPower = get(oldPowerId);
            PowerType oldPowerType = component.getPowerType(oldPower);

            if (oldPower.toJson().equals(newPower.toJson())) {
                continue;
            }

            Apoli.LOGGER.warn("Mismatched data fields of power \"{}\" from entity {}! Updating...", oldPowerId, entity.getName().getString());
            mismatch = true;

            for (Identifier source : component.getSources(oldPower)) {
                component.removePower(oldPower, source);
                component.addPower(newPower, source);
            }

            PowerType newPowerType = component.getPowerType(newPower);
            if (oldPowerType.getClass().isAssignableFrom(newPowerType.getClass())) {
                //  Transfer the data of the old power to the new power if the old power is an instance of the new power
                Apoli.LOGGER.info("Successfully transferred old data of power \"{}\"!", oldPowerId);
                newPowerType.fromTag(oldPowerType.toTag());
            } else {
                //  Output a warning that the data of the old power couldn't be transferred to the new power. This usually
                //  occurs if the power no longer uses the same power type as it used to
                Apoli.LOGGER.warn("Couldn't transfer old data of power \"{}\", as it's using a different power type!", oldPowerId);
            }

        }

        if (init || mismatch) {

            if (mismatch) {
                Apoli.LOGGER.info("Finished updating power data of entity {}.", entity.getName().getString());
            }

            component.sync();

        }

    }

    private void readSuperOrNormalPower(String packName, Identifier powerId, JsonObject powerJson) {

        Power power = Power.fromJson(powerId, powerJson);
        if (power.isMultiple()) {

            Set<SubPower> subPowers = new ObjectLinkedOpenHashSet<>();
            powerJson.asMap().forEach((key, jsonElement) -> {

                if (shouldIgnoreField(key)) {
                    return;
                }

                try {

                    if (!(jsonElement instanceof JsonObject subPowerJson)) {
                        throw new JsonSyntaxException("Expected a JSON object");
                    }

                    Identifier subPowerId = Identifier.of(powerId + "_" + key);
                    SubPower subPower = new SubPower(powerId, key, Power.fromJson(subPowerId, subPowerJson));

                    if (this.validateSubPower(packName, subPower, subPowerJson)) {
                        subPowers.add(subPower);
                    }

                }

                catch (Exception e) {
                    Apoli.LOGGER.error("There was a problem reading sub-power \"{}\" in power \"{}\": {}", key, powerId, e.getMessage());
                }

            });

            MultiplePower multiplePower = new MultiplePower(power, subPowers);
            this.validatePower(packName, multiplePower, powerJson);

        }

        else {
            this.validatePower(packName, power, powerJson);
        }

    }

    private boolean validateSubPower(String packName, SubPower subPower, JsonObject subPowerJson) {

        if (!ResourceConditionsImpl.applyResourceConditions(subPowerJson, directoryName, subPower.getId(), Calio.getDynamicRegistries().orElse(null))) {
            this.onReject(packName, subPower.getId());
            return false;
        }

        else if (subPower.isMultiple()) {
            throw new IllegalStateException("Using the 'multiple' power type in sub-powers is not allowed!");
        }

        else {
            return this.validatePower(packName, subPower, subPowerJson);
        }

    }

    private boolean validatePower(String packName, Power power, JsonObject powerJson) {

        Identifier powerId = power.getId();

        int currLoadingPriority = JsonHelper.getInt(powerJson, "loading_priority", 0);
        int prevLoadingPriority = LOADING_PRIORITIES.getOrDefault(powerId, 0);

        if (!contains(powerId)) {
            this.finishReadingPower(PowerManager::register, powerId, power, powerJson, currLoadingPriority);
            return true;
        }

        else if (currLoadingPriority < prevLoadingPriority) {

            Apoli.LOGGER.warn("Overriding power \"{}\" (with prev. loading priority of {}) with a higher loading priority of {} from data pack [{}]!", powerId, prevLoadingPriority, currLoadingPriority, packName);
            this.finishReadingPower(PowerManager::update, powerId, power, powerJson, currLoadingPriority);

            return true;

        }

        else {
            return power.isSubPower();
        }

    }

    private <T extends Power> void finishReadingPower(BiFunction<Identifier, T, T> powerTypeProcessor, Identifier powerId, T power, JsonObject jsonObject, int priority) {

        Identifier powerFactoryId = power.getFactoryInstance().getFactory().getSerializerId();
        boolean subPower = power.isSubPower();

        powerTypeProcessor.apply(powerId, power);
        LOADING_PRIORITIES.put(powerId, priority);

        handleAdditionalData(powerId, powerFactoryId, subPower, jsonObject, power);
        PostPowerLoadCallback.EVENT.invoker().onPostPowerLoad(powerId, powerFactoryId, subPower, jsonObject, power);

    }

    private static Power register(Identifier id, Power power) {

        if (powersByIdBuilder == null) {
            return power;
        }

        else if (powersByIdBuilder.containsKey(id)) {
            throw new IllegalArgumentException("Tried to register duplicate power with ID: \"" + id + "\"");
        }

        else {

            if (disabledPowersBuilder != null) {
                disabledPowersBuilder.remove(id);
            }

            powersByIdBuilder.put(id, power);
            return power;

        }

    }

    private static Power update(Identifier id, Power power) {

        if (powersByIdBuilder == null) {
            return power;
        }

        Power oldPower = remove(id);
        if (oldPower instanceof MultiplePower oldMultiplePower) {
            oldMultiplePower.getSubPowers().forEach(subPower -> remove(subPower.getId()));
        }

        PowerOverrideCallback.EVENT.invoker().onPowerOverride(id);
        return register(id, power);

    }

    private static Power remove(Identifier id) {

        if (powersByIdBuilder != null) {
            return powersByIdBuilder.remove(id);
        }

        else {
            return null;
        }

    }

    public static void disable(Identifier id) {

        if (disabledPowersBuilder == null) {
            return;
        }

        remove(id);
        disabledPowersBuilder.add(id);

    }

    private static void startBuild() {

        PowerClearCallback.EVENT.invoker().onPowerClear();

        building = true;
        validated = false;

        powersById = ImmutableMap.of();
        powersByIdBuilder = new HashMap<>();

        disabledPowers = ImmutableSet.of();
        disabledPowersBuilder = new HashSet<>();

    }

    /**
     *  Validates all powers if the power manager is currently building. The power manager will only (re)build on resource reload.
     */
    public static void validate() {

        if (building && powersByIdBuilder == null) {
            return;
        }

        Iterator<Map.Entry<Identifier, Power>> powerTypeIterator = powersByIdBuilder.entrySet().iterator();
        Apoli.LOGGER.info("Validating {} powers...", size());

        while (powerTypeIterator.hasNext()) {

            Map.Entry<Identifier, Power> powerTypeEntry = powerTypeIterator.next();
            validated = true;

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

    private static void endBuild() {

        if (powersByIdBuilder == null) {
            Apoli.LOGGER.warn("Couldn't build power manager, as it has no entry builder!");
        }

        else {

            powersById = ImmutableMap.copyOf(powersByIdBuilder);

            if (disabledPowersBuilder != null) {
                disabledPowers = ImmutableSet.copyOf(disabledPowersBuilder);
            }

        }

        powersByIdBuilder = null;
        building = false;

    }

    public void send(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new SyncPowerTypesS2CPacket(powersById));
    }

    @Environment(EnvType.CLIENT)
    public static void receive(SyncPowerTypesS2CPacket packet, ClientPlayNetworking.Context context) {
        startBuild();
        packet.powersById().forEach(PowerManager::register);
        endBuild();
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
        return Optional.ofNullable(((building ? ImmutableMap.copyOf(powersByIdBuilder) : powersById).get(id)));
    }

    public static Power get(Identifier id) {
        return getOptional(id).orElseThrow(() -> new IllegalArgumentException("Couldn't get power from ID \"" + id + "\", as it wasn't registered!"));
    }

    public static Set<Map.Entry<Identifier, Power>> entrySet() {
        return (building ? ImmutableMap.copyOf(powersByIdBuilder) : powersById).entrySet();
    }

    public static Collection<Power> values() {
        return (building ? ImmutableMap.copyOf(powersByIdBuilder) : powersById).values();
    }

    public static Stream<Identifier> streamIds() {
        return (building ? ImmutableMap.copyOf(powersByIdBuilder) : powersById)
            .keySet()
            .stream();
    }

    public static void forEach(BiConsumer<Identifier, Power> biConsumer) {
        (building ? powersByIdBuilder : powersById).forEach(biConsumer);
    }

    public static boolean isDisabled(Identifier id) {
        return (building ? ImmutableSet.copyOf(disabledPowersBuilder) : disabledPowers).contains(id);
    }

    public static boolean contains(Identifier id) {
        return (building ? ImmutableMap.copyOf(powersByIdBuilder) : powersById).containsKey(id);
    }

    public static int size() {
        return (building ? ImmutableMap.copyOf(powersByIdBuilder) : powersById).size();
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
