package io.github.apace100.apoli.screen;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

@Environment(EnvType.CLIENT)
public class ApoliScreenHandler {

    public static void registerAll() {
        HandledScreens.<DynamicContainerScreenHandler, CottonInventoryScreen<DynamicContainerScreenHandler>>register(ApoliScreenHandlerType.DYNAMIC_CONTAINER, CottonInventoryScreen::new);
    }

}
