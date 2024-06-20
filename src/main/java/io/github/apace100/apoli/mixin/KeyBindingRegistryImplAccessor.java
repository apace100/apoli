package io.github.apace100.apoli.mixin;

import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = KeyBindingRegistryImpl.class, remap = false)
public interface KeyBindingRegistryImplAccessor {

    @Accessor("MODDED_KEY_BINDINGS")
    static List<KeyBinding> getModdedKeyBindings() {
        throw new AssertionError();
    }

    @Mutable
    @Accessor("MODDED_KEY_BINDINGS")
    static void setModdedKeyBindings(List<KeyBinding> keys) {
        throw new AssertionError();
    }

}
