package io.github.apace100.apoli.util;

import io.github.apace100.apoli.mixin.KeyBindingAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class KeyBindingUtil {

    /**
     *  Get the localized name of the keybind from the specified ID. If no such keybind exists or if the keybind is
     *  not bound to any key, use the specified ID instead.
     *
     *  @param translationKey   The translation key of the keybind to get its localized bound key name of.
     *  @return                 Either a {@linkplain Text text} that is localized, or a {@linkplain net.minecraft.text.TranslatableTextContent translatable text}
     *                              that contains the specified translation key.
     */
    public static MutableText getLocalizedName(String translationKey) {

        KeyBinding keyBinding = KeyBindingAccessor.getKeysById().get(translationKey);
        if (keyBinding == null || keyBinding.isUnbound()) {
            return Text.translatable(translationKey);
        }

        return keyBinding.getBoundKeyLocalizedText().copy();

    }

}
