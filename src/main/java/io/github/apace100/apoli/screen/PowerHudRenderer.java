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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PowerHudRenderer implements GameHudRender {

    @Override
    @Environment(EnvType.CLIENT)
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

        int barWidth = 71;
        int barHeight = 8;
        int iconSize = 8;

        //  Used for selecting bars/icons depending on the specified bar/icon index
        int barIndexOffset = barHeight + 2;
        int iconIndexOffset = iconSize + 1;

        //  Get and sort the HUD powers
        List<HudRendered> hudRenderedPowers = component.getPowers()
            .stream()
            .filter(p -> p instanceof HudRendered)
            .map(p -> (HudRendered) p)
            .filter(hudRendered -> hudRendered.getRenderSettings().filter(player).findFirst().isPresent())
            .sorted(Comparator.comparing(hudRendered -> hudRendered.getRenderSettings().order()))
            .sorted(Comparator.comparing(hudRendered -> hudRendered.getRenderSettings().filter(player).findFirst().get().spriteLocation()))
            .toList();

        for (HudRendered hudRenderedPower : hudRenderedPowers) {

            //  Get the HUD render settings of the HUD power. Skip the drawing process if the HUD of the HUD power
            //  is specified to not render or if the condition is not fulfilled by the player
            HudRender hudRender = hudRenderedPower.getRenderSettings();
            if (!hudRenderedPower.shouldRender()) {
                continue;
            }

            Optional<HudRender.Inner> opt$innerHudRender = hudRender.filter(player).findFirst();

            //  The below shouldn't happen as we're filtering based on first being present
            //  but you can never be so sure.
            if (opt$innerHudRender.isEmpty()) {
                continue;
            }

            HudRender.Inner innerHudRender = opt$innerHudRender.get();

            //  Get the identifier of the sprite sheet and draw the base texture of the bar
            Identifier currentSpriteLocation = innerHudRender.spriteLocation();
            context.drawTexture(currentSpriteLocation, x, y, 0, 0, barWidth, 5);

            //  Get the V coordinate for the bar and icon and the U coordinate for the icon
            int barV = barHeight + innerHudRender.barIndex() * barIndexOffset;
            int iconU = (barWidth + 2) + innerHudRender.iconIndex() * iconIndexOffset;

            //  Get the fill portion of the bar
            float barFill = hudRenderedPower.getFill();
            if (innerHudRender.inverted()) {
                barFill = 1f - barFill;
            }

            //  Draw the fill portion of the bar
            int barFillWidth = (int) (barFill * barWidth);
            context.drawTexture(currentSpriteLocation, x, y - 2, 0, barV, barFillWidth, barHeight);

            //  Draw the icon of the bar
            context.drawTexture(currentSpriteLocation, x - iconSize - 2, y - 2, iconU, barV, iconSize, iconSize);
            y -= 8;

        }

    }

}