package io.github.apace100.apoli.util;

import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.mixin.SpriteAtlasTextureAccessor;
import io.github.apace100.apoli.mixin.TextureManagerAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.*;
import net.minecraft.util.Identifier;

import java.io.IOException;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class TextureUtil {

    public static final Identifier GUI_ATLAS_TEXTURE = new Identifier("textures/atlas/gui.png");

    /**
     *  <p>Tries loading the texture that corresponds with the specified {@link Identifier}.</p>
     *
     *  @param id   the {@link Identifier} of the texture to load
     *  @return     the {@link Identifier} of the texture wrapped in a {@link DataResult}
     */
    public static DataResult<Identifier> tryLoadingTexture(Identifier id) {
        return tryLoadingTexture(id, false);
    }

    /**
     *  <p>Tries loading the texture that corresponds with the specified {@link Identifier}.</p>
     *
     *  @param id               the {@link Identifier} of the texture to load
     *  @param exceptionOnly    determines whether to only include the exception if it results in an error
     *  @return                 the {@link Identifier} of the texture wrapped in a {@link DataResult}
     */
    public static DataResult<Identifier> tryLoadingTexture(Identifier id, boolean exceptionOnly) {

        TextureManagerAccessor textureManagerAccessor = (TextureManagerAccessor) MinecraftClient.getInstance().getTextureManager();

        AbstractTexture texture = textureManagerAccessor.getTextures().get(id);
        StringBuilder err = new StringBuilder();

        if (texture != null) {

            DataResult<Identifier> result = texture != MissingSprite.getMissingSpriteTexture()
                ? DataResult.success(id)
                : DataResult.error(() -> "Texture \"" + id + "\" does not exist!");

            textureManagerAccessor.callCloseTexture(id, texture);
            return result;

        }

        try {
            texture = new ResourceTexture(id);
            texture.load(textureManagerAccessor.getResourceContainer());
        } catch (IOException io) {

            if (id != TextureManager.MISSING_IDENTIFIER) {

                err.append(io);

                if (!exceptionOnly) {
                    err.insert(0, "Failed to load texture \"" + id + "\": ");
                }

            }

            texture = MissingSprite.getMissingSpriteTexture();

        }

        AbstractTexture prevTexture = textureManagerAccessor.getTextures().put(id, texture);
        if (prevTexture != null && prevTexture != MissingSprite.getMissingSpriteTexture()) {
            textureManagerAccessor.callCloseTexture(id, prevTexture);
        }

        if (texture != MissingSprite.getMissingSpriteTexture()) {
            textureManagerAccessor.callCloseTexture(id, texture);
        }

        return err.isEmpty()
            ? DataResult.success(id)
            : DataResult.error(err::toString);

    }

    /**
     *  <p>Tries loading the sprite that corresponds with the first {@link Identifier} from the
     *  texture atlas that corresponds with the second {@link Identifier}.</p>
     *
     *  @param spriteId     the {@link Identifier} of the sprite to load
     *  @param atlasId      the {@link Identifier} of the texture atlas
     *  @return             the {@link Identifier} of the sprite wrapped in a {@link DataResult}
     */
    public static DataResult<Identifier> tryLoadingSprite(Identifier spriteId, Identifier atlasId) {

        TextureManagerAccessor textureManagerAccessor = (TextureManagerAccessor) MinecraftClient.getInstance().getTextureManager();
        DataResult<Identifier> loadingResult = tryLoadingTexture(atlasId, true);

        if (loadingResult.result().isEmpty()) {
            return loadingResult.mapError(err -> "Failed to load atlas \"%s\": %s".formatted(atlasId, err));
        }

        try (AbstractTexture texture = textureManagerAccessor.getTextures().get(atlasId)) {

            if (!(texture instanceof SpriteAtlasTexture atlasTexture)) {
                throw new IllegalArgumentException("Identifier \"" + atlasId + "\" does not refer to an atlas texture!");
            }

            Sprite missingSprite = ((SpriteAtlasTextureAccessor) atlasTexture).getMissingSprite();
            Sprite sprite = atlasTexture.getSprite(spriteId);

            if (sprite == missingSprite) {
                throw new IllegalArgumentException("Sprite \"" + spriteId + "\" does not exist in atlas \"" + atlasId + "\"!");
            }

            return DataResult.success(spriteId);

        } catch (Throwable t) {
            return DataResult.error(() -> "Failed to load sprite \"%s\" from atlas \"%s\": %s".formatted(spriteId, atlasId, t));
        }

    }

}
