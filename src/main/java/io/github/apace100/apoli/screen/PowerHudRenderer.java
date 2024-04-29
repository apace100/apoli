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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Environment(EnvType.CLIENT)
public class PowerHudRenderer implements GameHudRender {

    private static final int BAR_WIDTH = 71;
    private static final int BAR_HEIGHT = 8;
    private static final int ICON_SIZE = 8;

    private static final int BAR_INDEX_OFFSET = BAR_HEIGHT + 2;
    private static final int ICON_INDEX_OFFSET = ICON_SIZE + 1;

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
            int bars = MathHelper.clamp((int) Math.ceil(livingVehicle.getMaxHealth() / 20.0F), 1, 3) - 1;
            yOffset += bars * 10;
        }

        x.set(((context.getScaledWindowWidth() / 2) + 20) + config.resourcesAndCooldowns.hudOffsetX);
        y.set((context.getScaledWindowHeight() - yOffset) + config.resourcesAndCooldowns.hudOffsetY);

        PowerHolderComponent.KEY.get(player).getPowers()
            .stream()
            .filter(p -> p instanceof HudRendered)
            .map(p -> (HudRendered) p)
            .filter(HudRendered::shouldRender)
            .map(h -> Map.entry(h, h.getRenderSettings().getChildOrSelf(player)))
            .filter(entry -> entry.getValue().isPresent())
            .sorted(Map.Entry.comparingByValue(Comparator.comparing(Optional::get)))
            .forEach(entry -> {

                HudRendered hudRendered = entry.getKey();
                HudRender hudRender = entry.getValue().get();

                //  Draw the background texture of the resource bar
                Identifier spriteLocation = hudRender.getSpriteLocation();
                context.drawTexture(spriteLocation, x.get(), y.get(), 0, 0, BAR_WIDTH, 5);

                int barV = BAR_HEIGHT + hudRender.getBarIndex() * BAR_INDEX_OFFSET;
                int iconU = (BAR_WIDTH + 2) + hudRender.getIconIndex() * ICON_INDEX_OFFSET;

                //  Draw the fill portion of the resource bar
                int barFillWidth = (int) ((hudRender.isInverted() ? 1.0F - hudRendered.getFill() : hudRendered.getFill()) * BAR_WIDTH);
                context.drawTexture(spriteLocation, x.get(), y.get() - 2, 0, barV, barFillWidth, BAR_HEIGHT);

                //  Draw the icon of the resource bar
                context.drawTexture(spriteLocation, x.get() - ICON_SIZE - 2, y.get() - 2, iconU, barV, ICON_SIZE, ICON_SIZE);
                y.getAndAdd(-8);

            });

    }

}
