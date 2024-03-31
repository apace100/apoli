package io.github.apace100.apoli.power;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.AtlasHolderContainer;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class OverlayPower extends Power {

    public static final Identifier ATLAS_TEXTURE = Apoli.identifier("textures/atlas/overlay.png");

    private final Identifier textureId;
    private final Identifier spriteId;

    private final DrawMode drawMode;
    private final DrawPhase drawPhase;

    private final float strength;
    private final float red;
    private final float green;
    private final float blue;

    private final boolean hideWithHud;
    private final boolean visibleInThirdPerson;

    private final int priority;

    public enum DrawMode {
        NAUSEA, TEXTURE
    }

    public enum DrawPhase {
        BELOW_HUD, ABOVE_HUD
    }

    public OverlayPower(PowerType<?> powerType, LivingEntity entity, Identifier textureId, Identifier spriteId, float red, float green, float blue, float strength, int priority, DrawMode drawMode, DrawPhase drawPhase, boolean hideWithHud, boolean visibleInThirdPerson) {
        super(powerType, entity);
        this.textureId = textureId;
        this.spriteId = spriteId;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.strength = strength;
        this.priority = priority;
        this.drawMode = drawMode;
        this.drawPhase = drawPhase;
        this.hideWithHud = hideWithHud;
        this.visibleInThirdPerson = visibleInThirdPerson;
    }

    public DrawPhase getDrawPhase() {
        return drawPhase;
    }

    public boolean shouldBeVisibleInThirdPerson() {
        return visibleInThirdPerson;
    }

    public boolean doesHideWithHud() {
        return hideWithHud;
    }

    public int getPriority() {
        return priority;
    }

    @Environment(EnvType.CLIENT)
    public boolean shouldRender(GameOptions options, DrawPhase targetDrawPhase) {
        return this.getDrawPhase() == targetDrawPhase
            && (!options.hudHidden || !this.doesHideWithHud())
            && (options.getPerspective().isFirstPerson() || this.shouldBeVisibleInThirdPerson());
    }

    @SuppressWarnings({"SwitchStatementWithTooFewBranches", "resource"})
    @Environment(EnvType.CLIENT)
    public void render() {

        if (textureId == null && spriteId == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();

        double width, height, x1, y1, x2, y2;
        float r, g, b, a, u1, u2, v1, v2;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        switch (drawMode) {
            case NAUSEA -> {

                RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);
                double stretch = MathHelper.lerp(strength, 2.0D, 1.0D);

                r = red * strength;
                g = green * strength;
                b = blue * strength;

                width = scaledWidth * stretch;
                height = scaledHeight * stretch;

                x1 = (scaledWidth - width) / 2.0D;
                y1 = (scaledHeight - height) / 2.0D;

                a = 1.0F;

            }
            default -> {

                RenderSystem.defaultBlendFunc();

                r = red;
                g = green;
                b = blue;

                width = scaledWidth;
                height = scaledHeight;

                x1 = 0;
                y1 = 0;

                a = strength;

            }
        }

        RenderSystem.setShaderColor(r, g, b, a);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);

        Identifier textureToDraw;

        x2 = x1 + width;
        y2 = y1 + height;

        if (spriteId != null) {

            Sprite sprite = ((AtlasHolderContainer) client).apoli$getOverlay().getSprite(spriteId);
            textureToDraw = sprite.getAtlasId();

            u1 = sprite.getMinU();
            u2 = sprite.getMaxU();

            v1 = sprite.getMinV();
            v2 = sprite.getMaxV();

        }

        else {

            textureToDraw = textureId;

            u1 = 0.0F;
            u2 = 1.0F;

            v1 = 1.0F;
            v2 = 0.0F;

        }

        RenderSystem.setShaderTexture(0, textureToDraw);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(x1, y1, -1.0D).texture(u1, v1).next();
        bufferBuilder.vertex(x1, y2, -1.0D).texture(u1, v2).next();
        bufferBuilder.vertex(x2, y2, -1.0D).texture(u2, v2).next();
        bufferBuilder.vertex(x2, y1, -1.0D).texture(u2, v1).next();

        tessellator.draw();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();

    }

    @Environment(EnvType.CLIENT)
    public static final class SpriteHolder extends SpriteAtlasHolder {

        public SpriteHolder(TextureManager manager) {
            super(manager, ATLAS_TEXTURE, Apoli.identifier("overlay"));
        }

        @Override
        public Sprite getSprite(Identifier objectId) {
            return super.getSprite(objectId);
        }

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("overlay"),
            new SerializableData()
                .add("texture", SerializableDataTypes.IDENTIFIER, null)
                .add("sprite", SerializableDataTypes.IDENTIFIER, null)
                .add("red", SerializableDataTypes.FLOAT, 1.0F)
                .add("green", SerializableDataTypes.FLOAT, 1.0F)
                .add("blue", SerializableDataTypes.FLOAT, 1.0F)
                .add("strength", SerializableDataTypes.FLOAT, 1.0F)
                .add("priority", SerializableDataTypes.INT, 1)
                .add("draw_mode", SerializableDataType.enumValue(DrawMode.class))
                .add("draw_phase", SerializableDataType.enumValue(DrawPhase.class))
                .add("hide_with_hud", SerializableDataTypes.BOOLEAN, true)
                .add("visible_in_third_person", SerializableDataTypes.BOOLEAN, false),
            data -> (powerType, entity) -> new OverlayPower(
                powerType,
                entity,
                data.get("texture"),
                data.get("sprite"),
                data.get("red"),
                data.get("green"),
                data.get("blue"),
                data.get("strength"),
                data.get("priority"),
                data.get("draw_mode"),
                data.get("draw_phase"),
                data.get("hide_with_hud"),
                data.get("visible_in_third_person")
            )
        ).allowCondition();
    }
}
