package io.github.apace100.apoli.registry;

import io.github.apace100.apoli.Apoli;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ApoliActivities {
    public static final Activity AVOID = register(Apoli.identifier("avoid"));
    public static final Activity FIGHT = register(Apoli.identifier("fight"));

    public static void register() {

    }

    private static Activity register(Identifier identifier) {
        return Registry.register(Registries.ACTIVITY, identifier, new Activity(identifier.toString()));
    }
}
