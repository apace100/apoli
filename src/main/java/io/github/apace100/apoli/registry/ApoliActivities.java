package io.github.apace100.apoli.registry;

import io.github.apace100.apoli.Apoli;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ApoliActivities {
    public static final Activity AVOID = register(Apoli.identifier("avoid"));

    public static void register() {

    }

    private static Activity register(Identifier identifier) {
        return Registry.register(Registry.ACTIVITY, identifier, new Activity(identifier.toString()));
    }
}
