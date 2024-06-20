package io.github.apace100.apoli.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.MultiJsonDataLoader;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class KeyBindingManager extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    public KeyBindingManager() {
        super(GSON, "keybinds");
    }

    @Override
    public Identifier getFabricId() {
        return Apoli.identifier("keybinds");
    }

    @Override
    protected void apply(Map<Identifier, List<JsonElement>> prepared, ResourceManager manager, Profiler profiler) {
        KeybindRegistry.clear();
        prepared.forEach((id, jel) -> jel.forEach(je -> {
            try {
                KeyBindingData key = ApoliDataTypes.KEYBINDING.read(je);
                if(!KeybindRegistry.contains(id)) {
                    KeybindRegistry.register(id, key);
                }
            } catch(Exception e) {
                Apoli.LOGGER.error("There was a problem reading a KeyBinding file: " + id.toString() + " (skipping): " + e.getMessage());
            }
        }));
        Apoli.LOGGER.info("Finished loading keybindings from data files. Registry contains {} keybindings.", KeybindRegistry.size());
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return ImmutableList.of(Apoli.identifier("powers"));
    }
}
