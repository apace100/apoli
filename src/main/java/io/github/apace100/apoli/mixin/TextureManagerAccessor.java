package io.github.apace100.apoli.mixin;

import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import java.util.Set;

@Mixin(TextureManager.class)
public interface TextureManagerAccessor {

    @Accessor
    ResourceManager getResourceContainer();

    @Accessor
    Map<Identifier, AbstractTexture> getTextures();

    @Accessor
    Set<TextureTickListener> getTickListeners();

    @Invoker
    void callCloseTexture(Identifier id, AbstractTexture texture);

}
