package io.github.apace100.apoli.power;

import com.google.gson.*;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.integration.*;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.NamespaceAlias;
import io.github.apace100.calio.data.MultiJsonDataLoader;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;

@SuppressWarnings("rawtypes")
public class PowerTypes extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {

    public static final Set<Identifier> DEPENDENCIES = new HashSet<>();
    public static final Set<String> LOADED_DATAPACK_NAMESPACES = new HashSet<>();

    private static final Identifier MULTIPLE = Apoli.identifier("multiple");
    private static final Identifier SIMPLE = Apoli.identifier("simple");

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    private static final HashMap<Identifier, Integer> LOADING_PRIORITIES = new HashMap<>();

    private static final HashMap<String, AdditionalPowerDataCallback> ADDITIONAL_DATA = new HashMap<>();

    public PowerTypes() {
        super(GSON, "powers");
    }

    @Override
    protected void apply(Map<Identifier, List<JsonElement>> loader, ResourceManager manager, Profiler profiler) {
        PowerTypeRegistry.reset();
        LOADING_PRIORITIES.clear();
        LOADED_DATAPACK_NAMESPACES.clear();
        LOADED_DATAPACK_NAMESPACES.addAll(manager.getAllNamespaces());
        PowerReloadCallback.EVENT.invoker().onPowerReload();
        PrePowerReloadCallback.EVENT.invoker().onPrePowerReload();
        loader.forEach((id, jel) -> {
            for (JsonElement je : jel) {
                try {
                    SerializableData.CURRENT_NAMESPACE = id.getNamespace();
                    SerializableData.CURRENT_PATH = id.getPath();
                    JsonObject jo = je.getAsJsonObject();

                    PrePowerLoadCallback.EVENT.invoker().onPrePowerLoad(id, jo);

                    Identifier factoryId = Identifier.tryParse(JsonHelper.getString(jo, "type"));
                    if (isMultiple(factoryId)) {
                        List<Identifier> subPowers = new LinkedList<>();
                        for (Map.Entry<String, JsonElement> entry : jo.entrySet()) {
                            if (entry.getKey().equals("type")
                                || entry.getKey().equals("loading_priority")
                                || entry.getKey().equals("name")
                                || entry.getKey().equals("description")
                                || entry.getKey().equals("hidden")
                                || entry.getKey().equals("condition")
                                || entry.getKey().equals("loading_condition")
                                || entry.getKey().toString().startsWith("$")
                                || ADDITIONAL_DATA.containsKey(entry.getKey())) {
                                continue;
                            }
                            Identifier subId = new Identifier(id.toString() + "_" + entry.getKey());
                            try {
                                readPower(subId, entry.getValue(), true);
                                subPowers.add(subId);
                            } catch (Exception e) {
                                Apoli.LOGGER.error("There was a problem reading sub-power \"" +
                                    subId.toString() + "\" in power file \"" + id.toString() + "\": " + e.getMessage());
                            }
                        }
                        MultiplePowerType superPower = (MultiplePowerType) readPower(id, je, false, MultiplePowerType::new);
                        if (superPower != null) {
                            superPower.setSubPowers(subPowers);
                            handleAdditionalData(id, factoryId, false, jo, superPower);
                            PostPowerLoadCallback.EVENT.invoker().onPostPowerLoad(id, factoryId, false, jo, superPower);
                        }
                    } else {
                        readPower(id, je, false);
                    }
                } catch (Exception e) {
                    Apoli.LOGGER.error("There was a problem reading power file " + id.toString() + " (skipping): " + e.getMessage());
                }
            }
        });
        PostPowerReloadCallback.EVENT.invoker().onPostPowerReload();
        LOADING_PRIORITIES.clear();
        LOADED_DATAPACK_NAMESPACES.clear();
        SerializableData.CURRENT_NAMESPACE = null;
        SerializableData.CURRENT_PATH = null;
        Apoli.LOGGER.info("Finished loading powers from data files. Registry contains " + PowerTypeRegistry.size() + " powers.");
    }

    private void readPower(Identifier id, JsonElement je, boolean isSubPower) {
        readPower(id, je, isSubPower, PowerType::new);
    }

    private boolean isLoadingConditionAllowed(Identifier id, JsonElement je, int priority) {
        try {
            boolean bl = ApoliDataTypes.LOADING_CONDITION.read(je).test();
            if (!bl && PowerTypeRegistry.contains(id) && LOADING_PRIORITIES.get(id) < priority) {
                PowerTypeRegistry.remove(id);
            }
            return bl;
        } catch (Exception e) {
            Apoli.LOGGER.error("There was a problem reading loading condition on power " + id.toString() + " (power won't be loaded): " + e.getMessage());
            return false;
        }
    }

    @Nullable
    private PowerType readPower(Identifier id, JsonElement je, boolean isSubPower,
                                BiFunction<Identifier, PowerFactory.Instance, PowerType> powerTypeFactory) {
        JsonObject jo = je.getAsJsonObject();
        Identifier factoryId = Identifier.tryParse(JsonHelper.getString(jo, "type"));

        int priority = JsonHelper.getInt(jo, "loading_priority", 0);
        if (jo.has("loading_condition") && !isLoadingConditionAllowed(id, jo.get("loading_condition"), priority)) {
            return null;
        }

        if(isMultiple(factoryId)) {
            factoryId = SIMPLE;
            if(isSubPower) {
                throw new JsonSyntaxException("Power type \"" + MULTIPLE.toString() + "\" may not be used for a sub-power of "
                    + "another \"" + MULTIPLE.toString() + "\" power.");
            }
        }
        Optional<PowerFactory> optionalFactory = ApoliRegistries.POWER_FACTORY.getOrEmpty(factoryId);
        if(!optionalFactory.isPresent()) {
            if(NamespaceAlias.hasAlias(factoryId)) {
                optionalFactory = ApoliRegistries.POWER_FACTORY.getOrEmpty(NamespaceAlias.resolveAlias(factoryId));
            }
            if(!optionalFactory.isPresent()) {
                throw new JsonSyntaxException("Power type \"" + factoryId.toString() + "\" is not defined.");
            }
        }
        PowerFactory.Instance factoryInstance = optionalFactory.get().read(jo);
        PowerType type = powerTypeFactory.apply(id, factoryInstance);
        String name = JsonHelper.getString(jo, "name", "");
        String description = JsonHelper.getString(jo, "description", "");
        boolean hidden = JsonHelper.getBoolean(jo, "hidden", false);
        if(hidden || isSubPower) {
            type.setHidden();
        }
        type.setTranslationKeys(name, description);
        if(!PowerTypeRegistry.contains(id)) {
            PowerTypeRegistry.register(id, type);
            LOADING_PRIORITIES.put(id, priority);
            if(!(type instanceof MultiplePowerType<?>)) {
                handleAdditionalData(id, factoryId, isSubPower, jo, type);
                PostPowerLoadCallback.EVENT.invoker().onPostPowerLoad(id, factoryId, isSubPower, jo, type);
            }
        } else {
            if(LOADING_PRIORITIES.get(id) < priority) {
                PowerTypeRegistry.update(id, type);
                LOADING_PRIORITIES.put(id, priority);
                if(!(type instanceof MultiplePowerType<?>)) {
                    handleAdditionalData(id, factoryId, isSubPower, jo, type);
                    PostPowerLoadCallback.EVENT.invoker().onPostPowerLoad(id, factoryId, isSubPower, jo, type);
                }
            }
        }
        return type;
    }

    private boolean isMultiple(Identifier id) {
        if(MULTIPLE.equals(id)) {
            return true;
        }
        if(NamespaceAlias.hasAlias(id)) {
            return MULTIPLE.equals(NamespaceAlias.resolveAlias(id));
        }
        return false;
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
        return new Identifier(Apoli.MODID, "powers");
    }

    public static void registerAdditionalData(String data, AdditionalPowerDataCallback callback) {
        if(ADDITIONAL_DATA.containsKey(data)) {
            Apoli.LOGGER.error("Apoli already contains a callback for additional data for the field \"" + data + "\".");
            return;
        }
        // TODO: Check whether any power factory uses that data field?
        ADDITIONAL_DATA.put(data, callback);
    }

    public static int getLoadingPriority(Identifier powerId) {
        if(!LOADING_PRIORITIES.containsKey(powerId)) {
            return Integer.MIN_VALUE;
        }
        return LOADING_PRIORITIES.get(powerId);
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return DEPENDENCIES;
    }
}
