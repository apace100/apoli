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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class PowerHudRenderer implements GameHudRender {

    private static final int BAR_WIDTH = 71;
    private static final int BAR_HEIGHT = 8;
    private static final int ICON_SIZE = 8;

    private static final int BAR_INDEX_OFFSET = BAR_HEIGHT + 2;
    private static final int ICON_INDEX_OFFSET = ICON_SIZE + 1;

    @Override
    public void render(DrawContext context, float delta) {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null) {
            return;
        }

        PowerHolderComponent component = PowerHolderComponent.KEY.get(player);
        int x = client.getWindow().getScaledWidth() / 2 + 20 + ((ApoliConfigClient) Apoli.config).resourcesAndCooldowns.hudOffsetX;
        int y = client.getWindow().getScaledHeight() - 47 + ((ApoliConfigClient) Apoli.config).resourcesAndCooldowns.hudOffsetY;

        Entity vehicle = player.getVehicle();
        if (vehicle instanceof LivingEntity || (player.isSubmergedIn(FluidTags.WATER) || player.getAir() < player.getMaxAir())) {
            int multiplier = (vehicle instanceof LivingEntity livingVehicle ? (int) (livingVehicle.getMaxHealth() / 20f) : 1);
            y -= 8 * multiplier;
        }

        //  Get and sort the HUD powers and its HUD render settings
        //  TODO: Improve handling of overriding inherited order value
        Map<HudRendered, HudRender> hudRenderedMap = component.getPowers()
            .stream()
            .filter(p -> p instanceof HudRendered)
            .map(p -> (HudRendered) p)
            .filter(HudRendered::shouldRender)
            .map(hudRendered -> Map.entry(hudRendered, hudRendered.getRenderSettings().getChildOrSelf(player)))
            .filter(e -> e.getValue().isPresent())
            .sorted(Map.Entry.comparingByValue(Comparator.comparing(Optional::get)))
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(), (hudRender, hudRender2) -> hudRender, LinkedHashMap::new));

        for (Map.Entry<HudRendered, HudRender> hudRenderedEntry : hudRenderedMap.entrySet()) {

            HudRendered hudRenderedPower = hudRenderedEntry.getKey();
            HudRender childHudRender = hudRenderedEntry.getValue();

            //  Get the identifier of the sprite sheet and draw the base texture of the bar
            Identifier currentSpriteLocation = childHudRender.getSpriteLocation();
            context.drawTexture(currentSpriteLocation, x, y, 0, 0, BAR_WIDTH, 5);

            //  Get the V coordinate for the bar and icon and the U coordinate for the icon
            int barV = BAR_HEIGHT + childHudRender.getBarIndex() * BAR_INDEX_OFFSET;
            int iconU = (BAR_WIDTH + 2) + childHudRender.getIconIndex() * ICON_INDEX_OFFSET;

            //  Get the fill portion of the bar
            float barFill = hudRenderedPower.getFill();
            if (childHudRender.isInverted()) {
                barFill = 1f - barFill;
            }

            //  Draw the fill portion of the bar
            int barFillWidth = (int) (barFill * BAR_WIDTH);
            context.drawTexture(currentSpriteLocation, x, y - 2, 0, barV, barFillWidth, BAR_HEIGHT);

            //  Draw the icon of the bar
            context.drawTexture(currentSpriteLocation, x - ICON_SIZE - 2, y - 2, iconU, barV, ICON_SIZE, ICON_SIZE);
            y -= 8;

        }

    }

}