package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.Apoli;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl.addCategory;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = KeyBindingRegistryImpl.class, remap = false)
public class KeyBindingRegistryImplMixin {

    @Inject(method = "registerKeyBinding", at = @At("HEAD"), cancellable = true)
    private static void registerKeyBinding(KeyBinding binding, CallbackInfoReturnable<KeyBinding> cir) {
        for (KeyBinding existingKeyBindings : KeyBindingRegistryImplAccessor.getModdedKeyBindings()) {
            if (existingKeyBindings == binding) {
                Apoli.LOGGER.warn("Attempted to register a key binding twice: " + binding.getTranslationKey());
            } else if (existingKeyBindings.getTranslationKey().equals(binding.getTranslationKey())) {
                Apoli.LOGGER.warn("Attempted to register two key bindings with equal translation key: " + binding.getTranslationKey() + "!");
            }
        }

        // This will do nothing if the category already exists.
        addCategory(binding.getCategory());
        List<KeyBinding> keyBindingList = KeyBindingRegistryImplAccessor.getModdedKeyBindings();
        keyBindingList.add(binding);
        KeyBindingRegistryImplAccessor.setModdedKeyBindings(keyBindingList);
        cir.setReturnValue(binding);
    }
}
