package io.github.apace100.apoli.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.HudRendered;
import io.github.apace100.apoli.util.ApoliConfigClient;
import io.github.apace100.apoli.util.HudRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PowerHudRenderer implements GameHudRender {

    @Override
    @Environment(EnvType.CLIENT)
    public void render(DrawContext context, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        PowerHolderComponent component = PowerHolderComponent.KEY.get(client.player);
        int x = client.getWindow().getScaledWidth() / 2 + 20 + ((ApoliConfigClient)Apoli.config).resourcesAndCooldowns.hudOffsetX;
        int y = client.getWindow().getScaledHeight() - 47 + ((ApoliConfigClient)Apoli.config).resourcesAndCooldowns.hudOffsetY;
        Entity vehicle = client.player.getVehicle();
        if(vehicle instanceof LivingEntity) {
            y -= 8 * (int)(((LivingEntity)vehicle).getMaxHealth() / 20f);
        }
        if(client.player.isSubmergedIn(FluidTags.WATER) || client.player.getAir() < client.player.getMaxAir()) {
            y -= 8;
        }
        int barWidth = 71;
        int barHeight = 8;
        int iconSize = 8;
        List<HudRendered> hudPowers = component.getPowers().stream().filter(p -> p instanceof HudRendered).map(p -> (HudRendered)p).sorted(
            Comparator.comparing(hudRenderedA -> hudRenderedA.getRenderSettings().getSpriteLocation())
        ).collect(Collectors.toList());
        //Identifier lastLocation = null;
        //RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        for (HudRendered hudPower : hudPowers) {
            HudRender render = hudPower.getRenderSettings();
            if(render.shouldRender(client.player) && hudPower.shouldRender()) {
                Identifier currentLocation = render.getSpriteLocation();
                /*if(currentLocation != lastLocation) {
                    RenderSystem.setShaderTexture(0, currentLocation);
                    lastLocation = currentLocation;
                }*/
                context.drawTexture(currentLocation, x, y, 0, 0, barWidth, 5);
                int v = 8 + render.getBarIndex() * 10;
                float fill = hudPower.getFill();
                if(render.isInverted()) {
                    fill = 1f - fill;
                }
                int w = (int)(fill * barWidth);
                context.drawTexture(currentLocation, x, y - 2, 0, v, w, barHeight);
                //setZOffset(getZOffset() + 1);
                context.drawTexture(currentLocation, x - iconSize - 2, y - 2, 73, v, iconSize, iconSize);
                //setZOffset(getZOffset() - 1);
                y -= 8;
            }
        }
    }
}