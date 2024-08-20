package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.PowerCraftingBook;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
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
import java.util.function.Predicate;

@Mixin(AnimatedResultButton.class)
public abstract class AnimatedResultButtonMixin {

    @Shadow
    public abstract RecipeEntry<?> currentRecipe();

    @Shadow
    private RecipeBook recipeBook;

    @ModifyReturnValue(method = "getTooltip", at = @At("RETURN"))
    private List<Text> apoli$appendRequiredRecipePowerTooltip(List<Text> original) {

        RecipeEntry<?> recipeEntry = this.currentRecipe();
        if (recipeEntry.value() instanceof PowerCraftingRecipe pcr && this.recipeBook instanceof PowerCraftingBook pcb && pcb.apoli$getPlayer() != null) {

            PowerHolderComponent component = PowerHolderComponent.KEY.get(pcb.apoli$getPlayer());
            Text powerTooltip = PowerManager.getOptional(pcr.powerId())
                .filter(Predicate.not(component::hasPower))
                .map(Power::getName)
                .map(name -> Text.translatable("tooltip.apoli.power_recipe.required_power", name).formatted(Formatting.RED))
                .orElse(null);

            if (powerTooltip != null) {
                original.add(Text.empty());
                original.add(powerTooltip);
            }

        }

        return original;

    }

}
