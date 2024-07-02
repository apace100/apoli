package io.github.apace100.apoli.screen;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.HudRendered;
import io.github.apace100.apoli.util.ApoliConfigClient;
import io.github.apace100.apoli.util.HudRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Environment(EnvType.CLIENT)
public class PowerHudRenderer implements GameHudRender {

    private final AtomicInteger x = new AtomicInteger();
    private final AtomicInteger y = new AtomicInteger();

    @Override
    public void render(DrawContext context, float delta) {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null || !(Apoli.config instanceof ApoliConfigClient config)) {
            return;
        }

        int yOffset = 49;
        if (player.isSubmergedIn(FluidTags.WATER) || player.getAir() < player.getMaxAir()) {
            yOffset += 10;
        }

        if (player.getVehicle() instanceof LivingEntity livingVehicle) {
            int bars = MathHelper.clamp((int) Math.ceil(livingVehicle.getMaxHealth() / 20.0), 1, 3) - 1;
            yOffset += bars * 10;
        }

        x.set(((context.getScaledWindowWidth() / 2) + 20) + config.resourcesAndCooldowns.hudOffsetX);
        y.set((context.getScaledWindowHeight() - yOffset) + config.resourcesAndCooldowns.hudOffsetY);

        PowerHolderComponent.KEY.get(player).getPowers()
            .stream()
            .filter(p -> p instanceof HudRendered)
            .map(p -> (HudRendered) p)
            .map(h -> Map.entry(h, h.getRenderSettings().getChildOrSelf(player)))
            .filter(e -> e.getValue().shouldRender())
            .sorted(Map.Entry.comparingByValue(HudRender::compareTo))
            .forEach(entry -> {

                HudRendered hudRendered = entry.getKey();
                HudRender hudRender = entry.getValue();

                if (hudRender.render(hudRendered, context, x.get(), y.get(), delta)) {
                    y.getAndAdd(-HudRender.BAR_HEIGHT);
                }

            });

    }

}
