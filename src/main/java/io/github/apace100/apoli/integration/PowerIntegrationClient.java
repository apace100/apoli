package io.github.apace100.apoli.integration;

import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.HudRendered;
import io.github.apace100.apoli.power.ModifyEnchantmentLevelPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class PowerIntegrationClient {

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(Active::integrateCallback);
        ClientEntityEvents.ENTITY_UNLOAD.register(ModifyEnchantmentLevelPower::integrateCallback);
        PostLoadTexturesCallback.EVENT.register(HudRendered::integrateOnClientReloadCallback);
    }

}
