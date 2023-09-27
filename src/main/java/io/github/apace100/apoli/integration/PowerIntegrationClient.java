package io.github.apace100.apoli.integration;

import io.github.apace100.apoli.power.Active;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class PowerIntegrationClient {

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(Active::integrateCallback);
    }

}
