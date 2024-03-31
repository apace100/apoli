package io.github.apace100.apoli.access;

import io.github.apace100.apoli.power.OverlayPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface AtlasHolderContainer {
    OverlayPower.SpriteHolder apoli$getOverlay();
}
