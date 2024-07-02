package io.github.apace100.apoli.power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.util.HudRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

public interface HudRendered {

    HudRender getRenderSettings();

    float getFill();
    boolean shouldRender();

    @Environment(EnvType.CLIENT)
    static void integrateOnClientReloadCallback(MinecraftClient client, boolean initialized) {

        if (client.player != null) {
            PowerHolderComponent.KEY.get(client.player).getPowers()
                .stream()
                .filter(p -> p instanceof HudRendered)
                .map(p -> ((HudRendered) p).getRenderSettings())
                .forEach(HudRender::reset);
        }

    }

}
