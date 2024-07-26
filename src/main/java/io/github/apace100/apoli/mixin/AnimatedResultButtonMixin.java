package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.PowerCraftingBook;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.NeoRecipePower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.recipe.PowerCraftingRecipe;
import net.minecraft.client.gui.screen.recipebook.AnimatedResultButton;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(AnimatedResultButton.class)
public abstract class AnimatedResultButtonMixin {

    @Shadow
    public abstract RecipeEntry<?> currentRecipe();

    @Shadow
    private RecipeBook recipeBook;

    @ModifyReturnValue(method = "getTooltip", at = @At("RETURN"))
    private List<Text> apoli$appendRequiredRecipePowerTooltip(List<Text> original) {

        RecipeEntry<?> recipeEntry = this.currentRecipe();
        if (!(recipeEntry.value() instanceof PowerCraftingRecipe powerRecipe) || !(this.recipeBook instanceof PowerCraftingBook recipeBook) || recipeBook.apoli$getPlayer() == null) {
            return original;
        }

        PowerHolderComponent component = PowerHolderComponent.KEY.get(recipeBook.apoli$getPlayer());
        PowerType<?> powerType = PowerTypeRegistry.getNullable(powerRecipe.powerId());

        if (powerType != null && !(component.getPower(powerType) instanceof NeoRecipePower)) {
            original.add(Text.empty());
            original.add(Text
                .translatable("tooltip.apoli.power_recipe.required_power", powerType.getName())
                .formatted(Formatting.RED));
        }

        return original;

    }

}
