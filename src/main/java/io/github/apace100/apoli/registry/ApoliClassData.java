package io.github.apace100.apoli.registry;

import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.ClassDataRegistry;

public class ApoliClassData {

    public static final ClassDataRegistry<PowerType> POWER_TYPE = ClassDataRegistry.getOrCreate(PowerType.class, "PowerType");

    public static void registerAll() {
        POWER_TYPE.addPackage("io.github.apace100.apoli.power.type");
    }

}
