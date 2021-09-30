package io.github.apace100.apoli.power;

import com.google.gson.*;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.integration.AdditionalPowerDataCallback;
import io.github.apace100.apoli.integration.PostPowerLoadCallback;
import io.github.apace100.apoli.integration.PowerReloadCallback;
import io.github.apace100.apoli.integration.PrePowerLoadCallback;
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

import java.util.*;
import java.util.function.BiFunction;

@SuppressWarnings("rawtypes")
public class PowerTypes extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {

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
        PowerReloadCallback.EVENT.invoker().onPowerReload();
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
                        superPower.setSubPowers(subPowers);
                        handleAdditionalData(id, factoryId, false, jo, superPower);
                        PostPowerLoadCallback.EVENT.invoker().onPostPowerLoad(id, factoryId, false, jo, superPower);
                    } else {
                        readPower(id, je, false);
                    }
                } catch (Exception e) {
                    Apoli.LOGGER.error("There was a problem reading power file " + id.toString() + " (skipping): " + e.getMessage());
                }
            }
        });
        LOADING_PRIORITIES.clear();
        SerializableData.CURRENT_NAMESPACE = null;
        SerializableData.CURRENT_PATH = null;
        Apoli.LOGGER.info("Finished loading powers from data files. Registry contains " + PowerTypeRegistry.size() + " powers.");
    }

    private void readPower(Identifier id, JsonElement je, boolean isSubPower) {
        readPower(id, je, isSubPower, PowerType::new);
    }

    private PowerType readPower(Identifier id, JsonElement je, boolean isSubPower,
                                BiFunction<Identifier, PowerFactory.Instance, PowerType> powerTypeFactory) {
        JsonObject jo = je.getAsJsonObject();
        Identifier factoryId = Identifier.tryParse(JsonHelper.getString(jo, "type"));
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
        int priority = JsonHelper.getInt(jo, "loading_priority", 0);
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
}
