package io.github.apace100.apoli.util;

import com.google.common.collect.Lists;
import io.github.apace100.apoli.mixin.KeyBindingRegistryImplAccessor;
import net.minecraft.client.option.KeyBinding;

import java.util.List;

public class KeyBindingRegistryImplExtension {

    public static KeyBinding[] removeAndProcess(KeyBinding[] keysAll, String... keyBindingKeys) {
        List<KeyBinding> moddedKeyBindings = KeyBindingRegistryImplAccessor.getModdedKeyBindings();
        List<KeyBinding> newKeysAll = Lists.newArrayList(keysAll);
        newKeysAll.removeAll(moddedKeyBindings);

        for (int i = 0; i < moddedKeyBindings.size(); i++) {
            for (String keyBinding : keyBindingKeys) {
                if(moddedKeyBindings.get(i).getTranslationKey().equals(keyBinding)) {
                    moddedKeyBindings.remove(i);
                    i--;
                }
            }
        }

        newKeysAll.addAll(moddedKeyBindings);
        return newKeysAll.toArray(new KeyBinding[0]);
    }

}
