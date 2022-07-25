package io.github.apace100.apoli.integration.emi;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.List;

public class ModifiedEmiCraftingRecipe extends EmiCraftingRecipe {
    private final EmiStack originalRecipeOutput;
    private final PowerType<?> powerType;
    private final @Nullable MutableText multiplePowerTypeName;

    public ModifiedEmiCraftingRecipe(List<EmiIngredient> input, EmiStack output, EmiStack originalRecipeOutput, Identifier originalRecipeIdentifier, PowerType<?> powerType, @Nullable MutableText multiplePowerTypeName, boolean shapeless) {
        super(input, output, new Identifier(powerType.getIdentifier().getNamespace(), powerType.getIdentifier().getPath()  + "/" + originalRecipeIdentifier.getNamespace() + "/" + originalRecipeIdentifier.getPath()), shapeless);
        this.originalRecipeOutput = originalRecipeOutput;
        this.powerType = powerType;
        this.multiplePowerTypeName = multiplePowerTypeName;
    }

    @Override
    public int getDisplayHeight() {
        return 72 + (10 * MinecraftClient.getInstance().textRenderer.wrapLines(getPowerName(), 118).size()) + 4;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        super.addWidgets(widgets);

        widgets.addSlot(originalRecipeOutput, 92, 44).output(true).appendTooltip(Text.translatable("emi.apoli.original_output").setStyle(Style.EMPTY.withColor(Formatting.WHITE).withItalic(true)));

        ApoliEmiPlugin.showPowerRequirement(widgets, powerType, getPowerName());
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean hideCraftable() {
        return !PowerHolderComponent.KEY.get(MinecraftClient.getInstance().player).hasPower(powerType) || !PowerHolderComponent.KEY.get(MinecraftClient.getInstance().player).getPower(powerType).isActive();
    }

    private MutableText getPowerName() {
        return multiplePowerTypeName != null ? multiplePowerTypeName : powerType.getName();
    }
}
