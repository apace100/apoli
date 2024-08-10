package io.github.apace100.apoli.access;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public interface OverlaySpriteHolder {
    Sprite apoli$getSprite(Identifier id);
}
