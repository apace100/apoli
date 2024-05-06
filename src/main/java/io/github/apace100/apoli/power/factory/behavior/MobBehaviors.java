package io.github.apace100.apoli.power.factory.behavior;

import io.github.apace100.apoli.power.factory.behavior.types.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.registry.Registry;

public class MobBehaviors {
    public static void register() {
        register(FleeMobBehavior.createFactory());
        register(FollowMobBehavior.createFactory());
        register(HostileMobBehavior.createFactory());
        register(LookMobBehavior.createFactory());
        register(PassiveMobBehavior.createFactory());
        register(RevengeMobBehavior.createFactory());
    }

    private static void register(MobBehaviorFactory<?> serializer) {
        Registry.register(ApoliRegistries.MOB_BEHAVIOR_FACTORY, serializer.getSerializerId(), serializer);
    }
}