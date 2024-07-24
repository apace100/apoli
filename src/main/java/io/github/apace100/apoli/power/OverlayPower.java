package io.github.apace100.apoli.power;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.OverlaySpriteHolder;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.TextureUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasHolder;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class OverlayPower extends Power {

    public static final Identifier ATLAS_TEXTURE = Apoli.identifier("textures/atlas/overlay.png");

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

    private boolean initRender = true;
    private boolean invalidTexture;

    public OverlayPower(PowerType<?> powerType, LivingEntity entity, Identifier spriteId, DrawMode drawMode, DrawPhase drawPhase, boolean hideWithHud, boolean visibleInThirdPerson, float strength, float red, float green, float blue, int priority) {
        super(powerType, entity);
        this.spriteId = spriteId;
        this.drawMode = drawMode;
        this.drawPhase = drawPhase;
        this.hideWithHud = hideWithHud;
        this.visibleInThirdPerson = visibleInThirdPerson;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.strength = strength;
        this.priority = priority;
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

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Environment(EnvType.CLIENT)
    public void render() {

        if (spriteId == null || !initRender && invalidTexture) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client instanceof OverlaySpriteHolder overlaySpriteHolder)) {
            return;
        }

        if (initRender) {

            this.invalidTexture = TextureUtil.tryLoadingSprite(spriteId, ATLAS_TEXTURE)
                .result()
                .isEmpty();
            this.initRender = false;

            if (invalidTexture) {
                Apoli.LOGGER.warn("Power \"{}\" references texture sprite \"{}\", which doesn't exist!", type.getIdentifier(), spriteId);
                return;
            }

        }

        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();

        float width, height, x1, y1, x2, y2, red, green, blue, alpha;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        switch (drawMode) {
            case NAUSEA -> {

                RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);
                float stretch = MathHelper.lerp(strength, 2.0F, 1.0F);

                red = this.red * strength;
                green = this.green * strength;
                blue = this.blue * strength;

                width = scaledWidth * stretch;
                height = scaledHeight * stretch;

                x1 = (scaledWidth - width) / 2.0F;
                y1 = (scaledHeight - height) / 2.0F;

                alpha = 1.0F;

            }
            default -> {

                RenderSystem.defaultBlendFunc();

                red = this.red;
                green = this.green;
                blue = this.blue;

                width = scaledWidth;
                height = scaledHeight;

                x1 = 0;
                y1 = 0;

                alpha = strength;

            }
        }

        Sprite sprite = overlaySpriteHolder.apoli$getSprite(spriteId);
        Identifier textureToDraw = sprite.getAtlasId();

        float minU = sprite.getMinU();
        float maxU = sprite.getMaxU();

        float minV = sprite.getMinV();
        float maxV = sprite.getMaxV();

        RenderSystem.setShaderColor(red, green, blue, alpha);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);

        x2 = x1 + width;
        y2 = y1 + height;

        RenderSystem.setShaderTexture(0, textureToDraw);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        bufferBuilder.vertex(x1, y1, -1.0F).texture(minU, minV);
        bufferBuilder.vertex(x1, y2, -1.0F).texture(minU, maxV);
        bufferBuilder.vertex(x2, y2, -1.0F).texture(maxU, maxV);
        bufferBuilder.vertex(x2, y1, -1.0F).texture(maxU, minV);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

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

    @Environment(EnvType.CLIENT)
    public static void integrateCallback(MinecraftClient client, boolean initialized) {
        PowerHolderComponent.getPowers(client.player, OverlayPower.class, true).forEach(p -> p.initRender = true);
    }

    public enum DrawMode {
        NAUSEA, TEXTURE
    }

    public enum DrawPhase {
        BELOW_HUD, ABOVE_HUD
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("overlay"),
            new SerializableData()
                .add("texture", SerializableDataTypes.IDENTIFIER, null)
                .addFunctionedDefault("sprite", SerializableDataTypes.IDENTIFIER, data -> data.get("texture"))
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
                data.get("sprite"),
                data.get("draw_mode"),
                data.get("draw_phase"),
                data.get("hide_with_hud"),
                data.get("visible_in_third_person"),
                data.get("strength"),
                data.get("red"),
                data.get("green"),
                data.get("blue"),
                data.get("priority")
            )
        ).allowCondition();
    }
}
