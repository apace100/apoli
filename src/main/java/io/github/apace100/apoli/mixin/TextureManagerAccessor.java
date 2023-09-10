package io.github.apace100.apoli.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(TextureManager.class)
@Environment(EnvType.CLIENT)
public interface TextureManagerAccessor {

    @Accessor
    Map<Identifier, AbstractTexture> getTextures();

    @Accessor
    ResourceManager getResourceContainer();

}
