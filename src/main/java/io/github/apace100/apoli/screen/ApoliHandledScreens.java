package io.github.apace100.apoli.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

@Environment(EnvType.CLIENT)
public class ApoliHandledScreens {

    public static void registerAll() {
        HandledScreens.register(ApoliScreenHandlerTypes.DYNAMIC_CONTAINER, DynamicContainerScreen::new);
    }

}
