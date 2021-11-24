package io.github.apace100.apoli.util;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.calio.data.ClassDataRegistry;

public class PowerPackageRegistry {

    @Deprecated
    public static void register(String pkg) {
        ClassDataRegistry.getOrCreate(Power.class, "Power").addPackage(pkg);
    }
}
