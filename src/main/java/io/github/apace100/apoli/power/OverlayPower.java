package io.github.apace100.apoli.power;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class OverlayPower extends Power {

    private final Identifier texture;
    private final float strength;
    private final float red;
    private final float green;
    private final float blue;
    private final DrawMode drawMode;
    private final DrawPhase drawPhase;
    private final boolean hideWithHud;
    private final boolean visibleInThirdPerson;

    public enum DrawMode {
        NAUSEA, TEXTURE
    }

    public enum DrawPhase {
        BELOW_HUD, ABOVE_HUD
    }

    public OverlayPower(PowerType<?> type, LivingEntity entity, Identifier texture, float strength, float red, float green, float blue, DrawMode drawMode, DrawPhase drawPhase, boolean hideWithHud, boolean visibleInThirdPerson) {
        super(type, entity);
        this.texture = texture;
        this.strength = strength;
        this.red = red;
        this.green = green;
        this.blue = blue;
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

    @Environment(EnvType.CLIENT)
    public void render() {
        MinecraftClient client = MinecraftClient.getInstance();
        int i = client.getWindow().getScaledWidth();
        int j = client.getWindow().getScaledHeight();

        double d, e, l, m, n;
        float g, h, k, a;

        switch(drawMode) {
            case NAUSEA:
                d = MathHelper.lerp(strength, 2.0D, 1.0D);
                g = red * strength;
                h = green * strength;
                k = blue * strength;
                e = (double)i * d;
                l = (double)j * d;
                m = ((double)i - e) / 2.0D;
                n = ((double)j - l) / 2.0D;
                a = 1.0F;
                break;
            case TEXTURE: default:
                g = red;
                h = green;
                k = blue;
                a = strength;
                e = i;
                l = j;
                m = 0;
                n = 0;
                break;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        switch (drawMode) {
            case NAUSEA:
                RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);
                break;
            case TEXTURE: default:
                RenderSystem.defaultBlendFunc();
                break;
        }
        RenderSystem.setShaderColor(g, h, k, a);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(m, n + l, -90.0D).texture(0.0F, 1.0F).next();
        bufferBuilder.vertex(m + e, n + l, -90.0D).texture(1.0F, 1.0F).next();
        bufferBuilder.vertex(m + e, n, -90.0D).texture(1.0F, 0.0F).next();
        bufferBuilder.vertex(m, n, -90.0D).texture(0.0F, 0.0F).next();
        tessellator.draw();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
