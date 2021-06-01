package io.github.apace100.apoli.power;

import com.google.gson.*;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.MultiJsonDataLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

import java.util.*;
import java.util.function.BiFunction;

@SuppressWarnings("rawtypes")
public class PowerTypes extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {

    public static String CURRENT_NAMESPACE = "";
    public static String CURRENT_PATH = "";

    private static final Identifier MULTIPLE = Apoli.identifier("multiple");
    private static final Identifier SIMPLE = Apoli.identifier("simple");

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    private final HashMap<Identifier, Integer> loadingPriorities = new HashMap<>();

    public PowerTypes() {
        super(GSON, "powers");
    }

    @Override
    protected void apply(Map<Identifier, List<JsonElement>> loader, ResourceManager manager, Profiler profiler) {
        PowerTypeRegistry.reset();
        loadingPriorities.clear();
        loader.forEach((id, jel) -> {
            jel.forEach(je -> {
                try {
                    CURRENT_NAMESPACE = id.getNamespace();
                    CURRENT_PATH = id.getPath();
                    JsonObject jo = je.getAsJsonObject();
                    Identifier factoryId = Identifier.tryParse(JsonHelper.getString(jo, "type"));
                    if(MULTIPLE.equals(factoryId)) {
                        List<Identifier> subPowers = new LinkedList<>();
                        for(Map.Entry<String, JsonElement> entry : jo.entrySet()) {
                            if( entry.getKey().equals("type")
                            ||  entry.getKey().equals("loading_priority")
                            ||  entry.getKey().equals("name")
                            ||  entry.getKey().equals("description")
                            ||  entry.getKey().equals("hidden")
                            ||  entry.getKey().equals("condition")) {
                                continue;
                            }
                            Identifier subId = new Identifier(id.toString() + "_" + entry.getKey());
                            try {
                                readPower(subId, entry.getValue(), true);
                                subPowers.add(subId);
                            } catch(Exception e) {
                                Apoli.LOGGER.error("There was a problem reading sub-power \"" +
                                    subId.toString() + "\" in power file \"" + id.toString() + "\": " + e.getMessage());
                            }
                        }
                        MultiplePowerType superPower = (MultiplePowerType)readPower(id, je, false, MultiplePowerType::new);
                        superPower.setSubPowers(subPowers);
                    } else {
                        readPower(id, je, false);
                    }
                } catch(Exception e) {
                    Apoli.LOGGER.error("There was a problem reading power file " + id.toString() + " (skipping): " + e.getMessage());
                }
            });
        });
        loadingPriorities.clear();
        CURRENT_NAMESPACE = null;
        CURRENT_PATH = null;
        Apoli.LOGGER.info("Finished loading powers from data files. Registry contains " + PowerTypeRegistry.size() + " powers.");
    }

    private void readPower(Identifier id, JsonElement je, boolean isSubPower) {
        readPower(id, je, isSubPower, PowerType::new);
    }

    private PowerType readPower(Identifier id, JsonElement je, boolean isSubPower,
                                BiFunction<Identifier, PowerFactory.Instance, PowerType> powerTypeFactory) {
        JsonObject jo = je.getAsJsonObject();
        Identifier factoryId = Identifier.tryParse(JsonHelper.getString(jo, "type"));
        if(MULTIPLE.equals(factoryId)) {
            factoryId = SIMPLE;
            if(isSubPower) {
                throw new JsonSyntaxException("Power type \"" + MULTIPLE.toString() + "\" may not be used for a sub-power of "
                    + "another \"" + MULTIPLE.toString() + "\" power.");
            }
        }
        Optional<PowerFactory> optionalFactory = ApoliRegistries.POWER_FACTORY.getOrEmpty(factoryId);
        if(!optionalFactory.isPresent()) {
            throw new JsonSyntaxException("Power type \"" + factoryId.toString() + "\" is not defined.");
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
            loadingPriorities.put(id, priority);
        } else {
            if(loadingPriorities.get(id) < priority) {
                PowerTypeRegistry.update(id, type);
                loadingPriorities.put(id, priority);
            }
        }
        return type;
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(Apoli.MODID, "powers");
    }

    private static <T extends Power> PowerType<T> register(String path, PowerType<T> type) {
        return new PowerTypeReference<>(new Identifier(Apoli.MODID, path));
        //return PowerTypeRegistry.register(new Identifier(Origins.MODID, path), type);
    }
}
