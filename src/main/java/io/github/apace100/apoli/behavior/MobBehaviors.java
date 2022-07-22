package io.github.apace100.apoli.behavior;

import io.github.apace100.apoli.behavior.types.FleeMobBehavior;
import io.github.apace100.apoli.behavior.types.PassiveMobBehavior;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.util.registry.Registry;

public class MobBehaviors {
    public static void register() {
        register(FleeMobBehavior.createFactory());
        register(PassiveMobBehavior.createFactory());
    }

    private static void register(BehaviorFactory<?> serializer) {
        Registry.register(ApoliRegistries.BEHAVIOR_FACTORY, serializer.getSerializerId(), serializer);
    }
}