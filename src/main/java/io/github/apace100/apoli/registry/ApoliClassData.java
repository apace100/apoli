package io.github.apace100.apoli.registry;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.calio.data.ClassDataRegistry;

public class ApoliClassData {

    public static void registerAll() {

        ClassDataRegistry<Power> power = ClassDataRegistry.getOrCreate(Power.class, "Power");
        power.addPackage("io.github.apace100.apoli.power");
    }
}
