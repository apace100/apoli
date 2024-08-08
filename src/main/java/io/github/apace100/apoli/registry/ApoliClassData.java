package io.github.apace100.apoli.registry;

import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.ClassDataRegistry;

public class ApoliClassData {

    public static void registerAll() {

        ClassDataRegistry<PowerType> power = ClassDataRegistry.getOrCreate(PowerType.class, "PowerType");
        power.addPackage("io.github.apace100.apoli.power.type");

    }

}
