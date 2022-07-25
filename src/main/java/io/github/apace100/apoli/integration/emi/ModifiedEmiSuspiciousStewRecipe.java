package io.github.apace100.apoli.integration.emi;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.recipe.special.EmiArmorDyeRecipe;
import dev.emi.emi.recipe.special.EmiSuspiciousStewRecipe;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModifiedEmiSuspiciousStewRecipe extends EmiSuspiciousStewRecipe {
    public final EmiStack modifiedOutput;
    private final PowerType<?> powerType;
    private final @javax.annotation.Nullable MutableText multiplePowerTypeName;

    public ModifiedEmiSuspiciousStewRecipe(EmiStack output, Identifier originalRecipeIdentifier, PowerType<?> powerType, @Nullable MutableText multiplePowerTypeName) {
        super(new Identifier(powerType.getIdentifier().getNamespace(), powerType.getIdentifier().getPath()  + "/" + originalRecipeIdentifier.getNamespace() + "/" + originalRecipeIdentifier.getPath()));
        this.modifiedOutput = output;
        this.powerType = powerType;
        this.multiplePowerTypeName = multiplePowerTypeName;
    }

    @Override
    public int getDisplayHeight() {
        return 72 + (10 * MinecraftClient.getInstance().textRenderer.wrapLines(getPowerName(), 118).size()) + 4;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(modifiedOutput);
    }

    @Override
    public SlotWidget getOutputWidget(int x, int y) {
        return new SlotWidget(modifiedOutput, x, y);
    }

    public SlotWidget getOriginalOutputWidget(int x, int y) {
        return super.getOutputWidget(x, y);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        super.addWidgets(widgets);

        widgets.add(getOriginalOutputWidget(92, 42).output(true).appendTooltip(Text.translatable("emi.apoli.original_output").setStyle(Style.EMPTY.withColor(Formatting.WHITE).withItalic(true))));

        ApoliEmiPlugin.showPowerRequirement(widgets, powerType, getPowerName());
    }

    private MutableText getPowerName() {
        return multiplePowerTypeName != null ? multiplePowerTypeName : powerType.getName();
    }
}
