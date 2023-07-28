package io.github.apace100.apoli.power;

import com.google.gson.*;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.integration.*;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.ApoliResourceConditions;
import io.github.apace100.apoli.util.IdentifierAlias;
import io.github.apace100.calio.data.MultiJsonDataLoader;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

@SuppressWarnings("rawtypes")
public class PowerTypes extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {

    public static final Set<Identifier> DEPENDENCIES = new HashSet<>();
    public static final Set<String> LOADED_NAMESPACES = new HashSet<>();

    private static final Identifier MULTIPLE = Apoli.identifier("multiple");
    private static final Identifier SIMPLE = Apoli.identifier("simple");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

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
        super(GSON, "powers");
    }

    @Override
    protected void apply(Map<Identifier, List<JsonElement>> prepared, ResourceManager manager, Profiler profiler) {

        Apoli.LOGGER.info("Loading powers from data files...");
        PowerTypeRegistry.reset();

        LOADING_PRIORITIES.clear();
        LOADED_NAMESPACES.clear();

        LOADED_NAMESPACES.addAll(manager.getAllNamespaces());

        PowerReloadCallback.EVENT.invoker().onPowerReload();
        PrePowerReloadCallback.EVENT.invoker().onPrePowerReload();

        //  Preload all powers
        prepared.forEach((powerId, jsonElements) -> {
            for (JsonElement jsonElement : jsonElements) {
                loadPower(powerId, powerId, jsonElement, true);
            }
        });

        //  Validate all preloaded powers
        PowerTypeRegistry.validatePreLoadedPowers(powerTypeRef -> {

            Identifier powerId = powerTypeRef.getIdentifier();
            JsonElement jsonElement = powerTypeRef.getJsonData();

            if (jsonElement != null) {
                loadPower(powerId, powerTypeRef.getFileId(), jsonElement, false);
            }

        });

        PostPowerReloadCallback.EVENT.invoker().onPostPowerReload();

        LOADING_PRIORITIES.clear();
        LOADED_NAMESPACES.clear();

        SerializableData.CURRENT_NAMESPACE = null;
        SerializableData.CURRENT_PATH = null;

        Apoli.LOGGER.info("Finished loading powers from data files. Registry contains " + PowerTypeRegistry.size() + " powers.");

    }

    private void loadPower(Identifier powerId, Identifier fileId, JsonElement jsonElement, boolean preLoad) {
        try {

            SerializableData.CURRENT_NAMESPACE = fileId.getNamespace();
            SerializableData.CURRENT_PATH = fileId.getPath();

            JsonObject jsonObject = jsonElement.getAsJsonObject();

            PrePowerLoadCallback.EVENT.invoker().onPrePowerLoad(powerId, jsonObject);
            Identifier powerFactoryId = new Identifier(JsonHelper.getString(jsonObject, "type"));

            //  If the specified power type ID is not considered multiple, load the power as a normal power
            if (!isMultiple(powerFactoryId)) {
                readPower(powerId, jsonElement, false, preLoad);
                return;
            }

            //  Load the sub-powers of the super-power
            List<Identifier> subPowers = new LinkedList<>();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {

                if (shouldIgnoreField(entry.getKey())) {
                    continue;
                }

                Identifier subPowerId = new Identifier(powerId + "_" + entry.getKey());
                try {
                    PowerType<?> subPower = readPower(subPowerId, entry.getValue(), true, preLoad);
                    if (subPower != null) {
                        subPowers.add(subPowerId);
                    }
                } catch (Exception e) {
                    Apoli.LOGGER.error("There was a problem reading sub-power \"" + subPowerId + "\" in power file \"" + powerId + "\": " + e.getMessage());
                }

            }

            //  Load the supposed super-power. If currently preloading, return early as the sub-powers
            //  will be added on validation phase
            PowerType<?> power = readPower(powerId, jsonElement, false, preLoad, MultiplePowerType::new);
            if (preLoad) {
                return;
            }

            //  Get the actual super-power. If the super-power does not exist, disable its sub-powers
            MultiplePowerType<?> superPower = getMultiplePower(power);
            if (superPower == null) {
                subPowers.forEach(PowerTypeRegistry::disable);
                return;
            }

            //  Add the sub-powers of the super-power and handle its additional data
            superPower.setSubPowers(subPowers);
            handleAdditionalData(powerId, powerFactoryId, false, jsonObject, superPower);

            PostPowerLoadCallback.EVENT.invoker().onPostPowerLoad(powerId, powerFactoryId, false, jsonObject, superPower);

        } catch (Exception e) {
            Apoli.LOGGER.error("There was a problem reading power file " + powerId.toString() + " (skipping): " + e.getMessage());
        }
    }

    @Nullable
    private PowerType<?> readPower(Identifier id, JsonElement jsonElement, boolean isSubPower, boolean preLoad) {
        return readPower(id, jsonElement, isSubPower, preLoad, PowerType::new);
    }

    @Nullable
    private PowerType<?> readPower(Identifier id, JsonElement jsonElement, boolean isSubPower, boolean preLoad, BiFunction<Identifier, PowerFactory<?>.Instance, PowerType<?>> powerTypeFactory) {

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Identifier fileId = new Identifier(SerializableData.CURRENT_NAMESPACE, SerializableData.CURRENT_PATH);
        Identifier powerFactoryId = new Identifier(JsonHelper.getString(jsonObject, "type"));

        int priority = JsonHelper.getInt(jsonObject, "loading_priority", 0);

        //  If currently preloading and the resource conditions evaluate to false, disable the power
        //  and return early
        if (preLoad && !ApoliResourceConditions.test(fileId, jsonObject)) {
            if (!PowerTypeRegistry.contains(id) || !PowerTypeRegistry.isPreLoaded(id)) {
                PowerTypeRegistry.disable(id);
            }
            return null;
        }

        if (isMultiple(powerFactoryId)) {
            powerFactoryId = SIMPLE;
            if (isSubPower) {
                throw new JsonSyntaxException("Power type \"" + MULTIPLE + "\" may not be used for a sub-power of another \"" + MULTIPLE + "\" power.");
            }
        }

        //  If currently preloading, add a reference to the power to the registry (that contains the ID of the power,
        //  the current ID (of the power file, not sub-power ID) and the JSON object for defining the power
        Integer previousPriority = LOADING_PRIORITIES.get(id);
        if (preLoad) {

            PowerTypeReference<?> powerTypeRef = new PowerTypeReference<>(id, fileId, jsonObject);
            PowerTypeRegistry.preLoad(powerTypeRef);

            return powerTypeRef;

        }

        //  Otherwise, register the power normally (if it's not registered yet)
        else if (!PowerTypeRegistry.isDisabled(fileId) && !PowerTypeRegistry.contains(id)) {
            return finishReadingPower(PowerTypeRegistry::register, powerTypeFactory, id, powerFactoryId, jsonObject, isSubPower, priority);
        }

        //  If the power is already registered, and its priority value is higher than the
        //  previously recorded priority value, override the power
        else if (PowerTypeRegistry.contains(id) && previousPriority < priority) {
            Apoli.LOGGER.warn("Overriding power " + id + " (previous priority: " + previousPriority + ") with a priority of " + priority + "!");
            return finishReadingPower(PowerTypeRegistry::update, powerTypeFactory, id, powerFactoryId, jsonObject, isSubPower, priority);
        }

        return null;

    }

    private PowerType<?> finishReadingPower(BiFunction<Identifier, PowerType<?>, PowerType<?>> powerProcessor, BiFunction<Identifier, PowerFactory<?>.Instance, PowerType<?>> powerTypeFactory, Identifier id, Identifier powerFactoryId, JsonObject jsonObject, boolean isSubPower, int priority) {

        //  Get the power type factory from the specified power type ID, or from an alias of the power type ID
        //  if the power type does not exist from the power type ID and if the power type ID has an alias
        Optional<PowerFactory> optionalPowerFactory = ApoliRegistries.POWER_FACTORY.getOrEmpty(powerFactoryId);
        if (optionalPowerFactory.isEmpty() && IdentifierAlias.hasAlias(powerFactoryId)) {
            optionalPowerFactory = ApoliRegistries.POWER_FACTORY.getOrEmpty(IdentifierAlias.resolveAlias(powerFactoryId));
        }

        //  Get the power type factory instance of the power type factory and read the JSON object. If the
        //  power type factory does not exist, throw an exception instead
        PowerFactory<?>.Instance powerFactoryInstance = optionalPowerFactory
            .orElseThrow(() -> new JsonSyntaxException("Power type \"" + powerFactoryId + "\" is not registered."))
            .read(jsonObject);

        //  Load the power using the power ID, power factory instance, JSON object
        //  and process it with the specified processor
        PowerType<?> powerType = loadPowerType(id, powerFactoryInstance, jsonObject, isSubPower, powerTypeFactory);
        powerProcessor.apply(id, powerType);

        //  Put the power in a loading priority map and handle its additional data (if it's not a multiple)
        LOADING_PRIORITIES.put(id, priority);
        if (!(powerType instanceof MultiplePowerType<?>)) {
            handleAdditionalData(id, powerFactoryId, isSubPower, jsonObject, powerType);
            PostPowerLoadCallback.EVENT.invoker().onPostPowerLoad(id, powerFactoryId, isSubPower, jsonObject, powerType);
        }

        return powerType;

    }

    private PowerType<?> loadPowerType(Identifier id, PowerFactory<?>.Instance powerFactoryInstance, JsonObject jsonObject, boolean isSubPower, BiFunction<Identifier, PowerFactory<?>.Instance, PowerType<?>> powerTypeFactory) {

        String name = JsonHelper.getString(jsonObject, "name", "");
        String description = JsonHelper.getString(jsonObject, "description", "");

        boolean hidden = JsonHelper.getBoolean(jsonObject, "hidden", false);

        PowerType<?> powerType = powerTypeFactory.apply(id, powerFactoryInstance);
        powerType.setTranslationKeys(name, description);

        if (hidden || isSubPower) {
            powerType.setHidden();
        }

        return powerType;


    }

    @Nullable
    private MultiplePowerType<?> getMultiplePower(PowerType<?> powerType) {
        if (powerType instanceof PowerTypeReference<?> ref && ref.getReferencedPowerType() instanceof MultiplePowerType<?> multiple) {
            return multiple;
        } else if (powerType instanceof MultiplePowerType<?> multiple) {
            return multiple;
        } else {
            return null;
        }
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

    private void handleAdditionalData(Identifier powerId, Identifier factoryId, boolean isSubPower, JsonObject jsonObject, PowerType<?> powerType) {
        ADDITIONAL_DATA.forEach((dataFieldName, callback) -> {
            if (jsonObject.has(dataFieldName)) {
                callback.readAdditionalPowerData(powerId, factoryId, isSubPower, jsonObject.get(dataFieldName), powerType);
            }
        });
    }

    @Override
    public Identifier getFabricId() {
        return Apoli.identifier("powers");
    }

    public static void registerAdditionalData(String data, AdditionalPowerDataCallback callback) {
        if (ADDITIONAL_DATA.containsKey(data)) {
            Apoli.LOGGER.error("Apoli already contains a callback for additional data for the field \"" + data + "\".");
            return;
        }
        //  TODO: Check whether any power factory uses that data field?
        ADDITIONAL_DATA.put(data, callback);
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return DEPENDENCIES;
    }

}
