package io.github.apace100.apoli.util;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.mixin.TextureManagerAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class TextureUtil {

    /**
     *  <p>Tries loading the specified {@linkplain Identifier texture ID}. If the texture exists, the specified
     *  {@linkplain Identifier texture ID} will be wrapped in an {@link Optional} and returned. Otherwise,
     *  {@link Optional#empty()} is returned.</p>
     *
     *  @param textureId    the {@linkplain Identifier ID} of the texture to try loading.
     */
    public static Optional<Identifier> tryLoadingTexture(@NotNull Identifier textureId) {

        TextureManagerAccessor textureManager = (TextureManagerAccessor) MinecraftClient.getInstance().getTextureManager();
        AbstractTexture texture = textureManager.getTextures().get(textureId);

        if (texture != null) {
            texture.close();
            return Optional.of(textureId);
        }

        try (ResourceTexture resourceTexture = new ResourceTexture(textureId)) {
            resourceTexture.load(textureManager.getResourceContainer());
        } catch (IOException e) {
            Apoli.LOGGER.error("Cannot load texture " + textureId + ": " + e);
            return Optional.empty();
        }

        return Optional.of(textureId);

    }

}
