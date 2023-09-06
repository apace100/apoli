package io.github.apace100.apoli.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

@Environment(EnvType.CLIENT)
public class ApoliScreenHandler {

    public static void registerAll() {
        HandledScreens.register(ApoliScreenHandlerType.DYNAMIC_CONTAINER, DynamicContainerScreen::new);
    }

}
