package io.github.apace100.apoli.integration;

import io.github.apace100.apoli.power.EntitySetPower;
import io.github.apace100.apoli.power.ModifyEnchantmentLevelPower;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

public class PowerIntegration {

    public static void register() {
        ServerEntityEvents.ENTITY_UNLOAD.register(ModifyEnchantmentLevelPower::integrateCallback);
        ServerEntityEvents.ENTITY_UNLOAD.register(EntitySetPower::integrateCallback);
    }

}
