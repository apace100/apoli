package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class OverrideHudTexturePower extends Power implements Prioritized<OverrideHudTexturePower> {

    private final Map<Identifier, Identifier> textureMapping;
    private final Identifier statusBarTexture;

    private final int priority;

    public OverrideHudTexturePower(PowerType<?> type, LivingEntity entity, Identifier statusBarTexture, Map<Identifier, Identifier> textureMapping, int priority) {
        super(type, entity);
        this.textureMapping = textureMapping;
        this.statusBarTexture = statusBarTexture;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public Identifier getStatusBarTexture() {
        return statusBarTexture;
    }

    @Environment(EnvType.CLIENT)
    public void drawHeartTexture(DrawContext context, InGameHud.HeartType heartType, int x, int y, int width, int height, boolean hardcore, boolean blinking, boolean half) {

        if (statusBarTexture == null) {

            Identifier texture = heartType.getTexture(hardcore, half, blinking);
            Identifier newTexture = textureMapping.getOrDefault(texture, texture);

            context.drawGuiTexture(newTexture, x, y, width, height);

        } else {

            int index = switch (heartType) {
                case CONTAINER -> 0;
                case NORMAL -> 2;
                case POISONED -> 4;
                case WITHERED -> 6;
                case ABSORBING -> 8;
                case FROZEN -> 9;
            };

            int v = hardcore ? 5 * 9 : 0;
            int u = heartType == InGameHud.HeartType.CONTAINER ? (blinking ? 1 : 0) : (half ? 1 : 0) + (blinking ? 2 : 0);

            u = 16 + (index * 2 + u) * 9;
            context.drawTexture(statusBarTexture, x, y, u, v, width, height);

        }

    }

    @Environment(EnvType.CLIENT)
    public void drawBarTexture(DrawContext context, Identifier texture, int x, int y, int u, int v, int width, int height, boolean hasProgress, OptionalInt uOffset, OptionalInt vOffset) {

        if (statusBarTexture == null) {
            Identifier newTexture = textureMapping.getOrDefault(texture, texture);
            context.drawGuiTexture(newTexture, x, y, width, height);
        } else {

            u += (uOffset.isPresent() ? (hasProgress ? uOffset.getAsInt() : 0) : 0);
            v += (vOffset.isPresent() ? (hasProgress ? vOffset.getAsInt() : 0) : 0);

            context.drawTexture(statusBarTexture, x, y, u, v, width, height);

        }

    }

    @Environment(EnvType.CLIENT)
    public void drawTexture(DrawContext context, Identifier texture, int x, int y, int u, int v, int width, int height) {

        if (statusBarTexture != null) {
            context.drawTexture(statusBarTexture, x, y, u, v, width, height);
        } else {
            Identifier newTexture = textureMapping.getOrDefault(texture, texture);
            context.drawGuiTexture(newTexture, x, y, width, height);
        }

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("status_bar_texture"),
            new SerializableData()
                .add("texture", SerializableDataTypes.IDENTIFIER, null)
                .add("texture_map", ApoliDataTypes.IDENTIFIER_MAP, new LinkedHashMap<>())
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (powerType, livingEntity) -> new OverrideHudTexturePower(
                powerType,
                livingEntity,
                data.get("texture"),
                data.get("texture_map"),
                data.get("priority")
            )
        ).allowCondition();
    }
}