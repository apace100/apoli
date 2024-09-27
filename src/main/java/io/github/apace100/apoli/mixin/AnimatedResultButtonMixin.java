package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.access.PowerCraftingObject;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.recipe.PowerCraftingRecipe;
import io.github.apace100.apoli.recipe.ModifiedCraftingRecipe;
import net.minecraft.client.gui.screen.recipebook.AnimatedResultButton;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
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

    @WrapOperation(method = {"renderWidget", "getTooltip", "appendClickableNarrations"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeEntry;value()Lnet/minecraft/recipe/Recipe;"))
    private Recipe<?> apoli$modifyEntryQuery(RecipeEntry<?> entry, Operation<Recipe<?>> original, @Share("originalEntry") LocalRef<RecipeEntry<?>> sharedOriginalEntry) {

        sharedOriginalEntry.set(entry);

        Identifier id = entry.id();
        Recipe<?> recipe = entry.value();

        if (recipe instanceof CraftingRecipe craftingRecipe && ModifiedCraftingRecipe.canModify(id, craftingRecipe, this.recipeBook)) {
            return new ModifiedCraftingRecipe(id, craftingRecipe);
        }

        else {
            return original.call(entry);
        }

    }

    @WrapOperation(method = {"renderWidget", "getTooltip", "appendClickableNarrations"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/Recipe;getResult(Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack apoli$modifyResultQuery(Recipe<?> recipe, RegistryWrapper.WrapperLookup wrapperLookup, Operation<ItemStack> original) {

        if (recipe instanceof ModifiedCraftingRecipe modifiedCraftingRecipe && this.recipeBook instanceof PowerCraftingObject pco) {
            return modifiedCraftingRecipe.getModifiedResult(wrapperLookup, pco.apoli$getPlayer()).getFirst();
        }

        else {
            return original.call(recipe, wrapperLookup);
        }

    }

    @ModifyReturnValue(method = "getTooltip", at = @At("RETURN"))
    private List<Text> apoli$appendRequiredRecipePowerTooltip(List<Text> original, @Share("originalEntry") LocalRef<RecipeEntry<?>> sharedOriginalEntry) {

        RecipeEntry<?> recipeEntry = sharedOriginalEntry.get() != null
            ? sharedOriginalEntry.get()
            : this.currentRecipe();

        if (recipeEntry.value() instanceof PowerCraftingRecipe pcr && this.recipeBook instanceof PowerCraftingObject pco && pco.apoli$getPlayer() != null) {

            PowerHolderComponent component = PowerHolderComponent.KEY.get(pco.apoli$getPlayer());
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
