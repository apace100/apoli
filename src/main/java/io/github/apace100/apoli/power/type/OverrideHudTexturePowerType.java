package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
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

//  TODO: Drop backwards compatibility with the old sprite sheet functionality -eggohito
public class OverrideHudTexturePowerType extends PowerType implements Prioritized<OverrideHudTexturePowerType> {

    private final Map<Identifier, Identifier> textureMapping;
    private final Identifier statusBarTexture;

    private final int priority;

    public OverrideHudTexturePowerType(Power power, LivingEntity entity, Identifier statusBarTexture, Map<Identifier, Identifier> textureMapping, int priority) {
        super(power, entity);
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

    public boolean shouldRender() {
        return this.getStatusBarTexture() != null
            || !textureMapping.isEmpty();
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
    public void drawTextureRegion(DrawContext context, Identifier texture, int width, int height, int minU, int minV, int legacyMinU, int legacyMinV, int x, int y, int maxU, int maxV) {

        if (statusBarTexture == null) {
            context.drawGuiTexture(textureMapping.getOrDefault(texture, texture), width, height, minU, minV, x, y, maxU, maxV);
        }

        else {
            context.drawTexture(statusBarTexture, x, y, legacyMinU, legacyMinV, maxU, maxV);
        }

    }

    @Environment(EnvType.CLIENT)
    public void drawTexture(DrawContext context, Identifier texture, int x, int y, int legacyU, int legacyV, int width, int height) {

        if (statusBarTexture == null) {
            context.drawGuiTexture(textureMapping.getOrDefault(texture, texture), x, y, width, height);
        }

        else {
            context.drawTexture(statusBarTexture, x, y, legacyU, legacyV, width, height);
        }

    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(Apoli.identifier("status_bar_texture"),
            new SerializableData()
                .add("texture", SerializableDataTypes.IDENTIFIER, null)
                .add("texture_map", ApoliDataTypes.IDENTIFIER_MAP, new LinkedHashMap<>())
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (power, entity) -> new OverrideHudTexturePowerType(
                power,
                entity,
                data.get("texture"),
                data.get("texture_map"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
