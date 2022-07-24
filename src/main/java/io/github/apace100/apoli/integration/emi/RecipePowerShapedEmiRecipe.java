package io.github.apace100.apoli.integration.emi;

import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.recipe.EmiShapedRecipe;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.MultiplePowerType;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import javax.annotation.Nullable;
import java.util.List;

public class RecipePowerShapedEmiRecipe extends EmiShapedRecipe {
    private final PowerType<?> powerType;
    @Nullable private final MutableText multiplePowerTypeName;

    public RecipePowerShapedEmiRecipe(ShapedRecipe recipe, PowerType<?> powerType, @Nullable MutableText multiplePowerTypeName) {
        super(recipe);
        this.powerType = powerType;
        this.multiplePowerTypeName = multiplePowerTypeName;
    }

    @Override
    public int getDisplayHeight() {
        return 74 + (10 * MinecraftClient.getInstance().textRenderer.wrapLines(getPowerName(), 118).size()) + 4;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void addWidgets(WidgetHolder widgets) {
        super.addWidgets(widgets);
        int colorValue;
        if (PowerHolderComponent.KEY.get(MinecraftClient.getInstance().player).getPower(powerType) == null) {
            colorValue = Formatting.DARK_GRAY.getColorValue();
        } else if (!PowerHolderComponent.KEY.get(MinecraftClient.getInstance().player).getPower(powerType).isActive()) {
            colorValue = Formatting.RED.getColorValue();
        } else {
            colorValue = Formatting.DARK_GREEN.getColorValue();
        }
        widgets.addTexture(ApoliEmiPlugin.REQUIRED_POWER_HEADING_BORDER, 0, 56);
        widgets.addText(Text.translatable("emi.apoli.required_power").asOrderedText(), 4, 59, colorValue, true);
        List<OrderedText> powerNameLines = MinecraftClient.getInstance().textRenderer.wrapLines(getPowerName(), widgets.getWidth() - 8);
        int y = 74;
        for (OrderedText line : powerNameLines) {
            widgets.addTexture(ApoliEmiPlugin.POWER_NAME_BORDER_MIDDLE, 0, y);
            widgets.addText(line, 4, y, Formatting.WHITE.getColorValue(), false);
            y += 10;
        }
        widgets.addTexture(ApoliEmiPlugin.POWER_NAME_BORDER_BOTTOM, 0, y);
    }

    private MutableText getPowerName() {
        return multiplePowerTypeName != null ? multiplePowerTypeName : powerType.getName();
    }
}
