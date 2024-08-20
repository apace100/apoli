package io.github.apace100.apoli.integration;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.EntitySetPowerType;
import io.github.apace100.apoli.power.type.ModifyEnchantmentLevelPowerType;
import io.github.apace100.apoli.power.type.ModifyTypeTagPowerType;
import io.github.apace100.apoli.power.type.PowerType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class PowerIntegration {

    public static void register() {

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> PowerHolderComponent.getPowerTypes(entity, PowerType.class, true).forEach(PowerType::onAdded));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> PowerHolderComponent.getPowerTypes(entity, PowerType.class, true).forEach(PowerType::onRemoved));

        ServerEntityEvents.ENTITY_UNLOAD.register(ModifyEnchantmentLevelPowerType::integrateCallback);
        ServerEntityEvents.ENTITY_UNLOAD.register(EntitySetPowerType::integrateUnloadCallback);
        ServerEntityEvents.ENTITY_LOAD.register(EntitySetPowerType::integrateLoadCallback);

        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register(ModifyTypeTagPowerType::registerStartServerReloadCallback);

    }

}
