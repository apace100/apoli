package io.github.apace100.apoli.util;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.mixin.GameOptionsAccessor;
import io.github.apace100.apoli.mixin.KeyBindingRegistryImplAccessor;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeybindRegistry {

    private static final HashMap<Identifier, KeyBindingData> idToKeyBinding = new HashMap<>();
    private static final List<Identifier> idList = new LinkedList<>();

    public static void register(Identifier id, KeyBindingData binding) {
        if (idToKeyBinding.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate keybinding id tried to register: '" + id.toString() + "'");
        }
        idToKeyBinding.put(id, binding);
    }

    public static void registerClient(Identifier id, KeyBindingData binding) {
        register(id, binding);

        // Get key from name
        InputUtil.Key key = InputUtil.Type.KEYSYM.map.values().stream().filter((akey -> akey.getTranslationKey().equals(binding.getKey()))).toList().get(0);

        // Get key from options.txt
        File file = MinecraftClient.getInstance().options.getOptionsFile();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Pattern pattern = Pattern.compile("key_" + binding.getName().getString() +":(.*)");
                Matcher matcher = pattern.matcher(line);
                Apoli.LOGGER.info(line);
                if (matcher.find()) {
                    String savedKey = matcher.group(1);
                    key = InputUtil.fromTranslationKey(savedKey);
                }
            }
        } catch (IOException e) {
            Apoli.LOGGER.error(e.getMessage());
        }

        KeyBinding keyBinding = new KeyBinding(binding.getName().getString(), InputUtil.Type.KEYSYM, key.getCode(), binding.getCategory());
        KeyBindingHelper.registerKeyBinding(keyBinding);
        ApoliClient.registerPowerKeybinding(binding.getTranslationKey(), keyBinding);
    }

    public static List<Identifier> getList() {
        return idList;
    }

    public static int size() {
        return idToKeyBinding.size();
    }

    public static Iterable<Map.Entry<Identifier, KeyBindingData>> entries() {
        return idToKeyBinding.entrySet();
    }

    public static KeyBindingData get(Identifier id) {
        if(!idToKeyBinding.containsKey(id)) {
            throw new IllegalArgumentException("Could not get keybinding from id '" + id.toString() + "', as it was not registered!");
        }
        return idToKeyBinding.get(id);
    }

    public static boolean contains(Identifier id) {
        return idToKeyBinding.containsKey(id);
    }

    public static void forEach(BiConsumer<Identifier, KeyBindingData> powerTypeBiConsumer) {
        idToKeyBinding.forEach(powerTypeBiConsumer::accept);
    }

    public static void clear() {
        List<KeyBinding> keyBindingList = KeyBindingRegistryImplAccessor.getModdedKeyBindings();
        idToKeyBinding.values().forEach((keyBinding -> {
            if(keyBinding.getTranslationKey() != null) {
                ApoliClient.idToKeyBindingMap.remove(keyBinding.getTranslationKey());
            }
        }));

        List<String> keybindKeys = new LinkedList<>();
        for (KeyBindingData value : idToKeyBinding.values()) {
            keybindKeys.add(value.getName().getString());
        }

        KeyBindingRegistryImplAccessor.setModdedKeyBindings(keyBindingList);
        ((GameOptionsAccessor)MinecraftClient.getInstance().options).setAllKeys(KeyBindingRegistryImplExtension.removeAndProcess(MinecraftClient.getInstance().options.allKeys, keybindKeys.toArray(new String[]{})));
        idToKeyBinding.clear();
    }

}
