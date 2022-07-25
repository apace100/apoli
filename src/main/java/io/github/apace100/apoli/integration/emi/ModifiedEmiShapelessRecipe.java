package io.github.apace100.apoli.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.MutableText;

import javax.annotation.Nullable;
import java.util.List;

public class ModifiedEmiShapelessRecipe extends ModifiedEmiCraftingRecipe {
    public ModifiedEmiShapelessRecipe(ShapelessRecipe recipe, EmiStack output, PowerType<?> powerType, @Nullable MutableText multiplePowerTypeName) {
        super(recipe.getIngredients().stream().map(EmiIngredient::of).toList(), output, EmiStack.of(recipe.getOutput()), recipe.getId(),  powerType, multiplePowerTypeName, true);

        for(int i = 0; i < this.input.size(); ++i) {
            CraftingInventory inv = new CraftingInventory(new ScreenHandler(null, -1) {
                public boolean canUse(PlayerEntity player) {
                    return false;
                }

                public ItemStack transferSlot(PlayerEntity player, int index) {
                    return null;
                }
            }, 3, 3);

            for(int j = 0; j < this.input.size(); ++j) {
                if (j != i && !this.input.get(j).isEmpty()) {
                    inv.setStack(j, this.input.get(j).getEmiStacks().get(0).getItemStack().copy());
                }
            }

            List<EmiStack> stacks = this.input.get(i).getEmiStacks();

            for (EmiStack stack : stacks) {
                inv.setStack(i, stack.getItemStack().copy());
                ItemStack remainder = recipe.getRemainder(inv).get(i);
                if (!remainder.isEmpty()) {
                    stack.setRemainder(EmiStack.of(remainder));
                }
            }
        }
    }
    public boolean canFit(int width, int height) {
        return this.input.size() <= width * height;
    }
}
