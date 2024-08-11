package io.github.apace100.apoli.integration;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.Active;
import io.github.apace100.apoli.power.type.ModifyEnchantmentLevelPowerType;
import io.github.apace100.apoli.power.type.OverlayPowerType;
import io.github.apace100.apoli.power.type.PowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class PowerIntegrationClient {

    public static void register() {

        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> PowerHolderComponent.getPowerTypes(entity, PowerType.class, true).forEach(PowerType::onAdded));
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> PowerHolderComponent.getPowerTypes(entity, PowerType.class, true).forEach(PowerType::onRemoved));

        ClientTickEvents.START_CLIENT_TICK.register(Active::integrateCallback);
        ClientEntityEvents.ENTITY_UNLOAD.register(ModifyEnchantmentLevelPowerType::integrateCallback);
        PostLoadTexturesCallback.EVENT.register(OverlayPowerType::integrateCallback);

    }

}
