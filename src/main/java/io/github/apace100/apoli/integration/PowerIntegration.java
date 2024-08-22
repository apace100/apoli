package io.github.apace100.apoli.integration;

import io.github.apace100.apoli.power.type.EntitySetPowerType;
import io.github.apace100.apoli.power.type.ModifyEnchantmentLevelPowerType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

public class PowerIntegration {

    public static void register() {
        ServerEntityEvents.ENTITY_UNLOAD.register(ModifyEnchantmentLevelPowerType::integrateCallback);
        ServerEntityEvents.ENTITY_UNLOAD.register(EntitySetPowerType::integrateUnloadCallback);
        ServerEntityEvents.ENTITY_LOAD.register(EntitySetPowerType::integrateLoadCallback);
    }

}
