package io.github.apace100.apoli.integration;

import io.github.apace100.apoli.power.EntitySetPower;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

public class PowerIntegration {

    public static void register() {
        //  TODO: Uncomment this after fixing the `modify_enchantment_level` power type -eggohito
//        ServerEntityEvents.ENTITY_UNLOAD.register(ModifyEnchantmentLevelPower::integrateCallback);
        ServerEntityEvents.ENTITY_UNLOAD.register(EntitySetPower::integrateUnloadCallback);
        ServerEntityEvents.ENTITY_LOAD.register(EntitySetPower::integrateLoadCallback);
    }

}
