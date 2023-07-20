package io.github.apace100.apoli.util.modifier;

import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;

import java.util.Locale;

public final class ModifierOperations {

    public static void registerAll() {
        for(ModifierOperation operation : ModifierOperation.values()) {
            Registry.register(ApoliRegistries.MODIFIER_OPERATION,
                new Identifier(operation.name().toLowerCase(Locale.ROOT)),
                operation);
        }
    }
}
