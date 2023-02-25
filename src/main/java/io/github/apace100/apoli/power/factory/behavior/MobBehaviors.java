package io.github.apace100.apoli.power.factory.behavior;

import io.github.apace100.apoli.power.factory.behavior.types.FleeMobBehavior;
import io.github.apace100.apoli.power.factory.behavior.types.FollowMobBehavior;
import io.github.apace100.apoli.power.factory.behavior.types.HostileMobBehavior;
import io.github.apace100.apoli.power.factory.behavior.types.PassiveMobBehavior;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.registry.Registry;

public class MobBehaviors {
    public static void register() {
        register(FleeMobBehavior.createFactory());
        register(FollowMobBehavior.createFactory());
        register(HostileMobBehavior.createFactory());
        register(PassiveMobBehavior.createFactory());
    }

    private static void register(BehaviorFactory<?> serializer) {
        Registry.register(ApoliRegistries.BEHAVIOR_FACTORY, serializer.getSerializerId(), serializer);
    }
}