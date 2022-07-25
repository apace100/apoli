package io.github.apace100.apoli.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.MutableText;
import org.apache.commons.compress.utils.Lists;

import javax.annotation.Nullable;
import java.util.List;

public class ModifiedEmiShapedRecipe extends ModifiedEmiCraftingRecipe {
    public ModifiedEmiShapedRecipe(ShapedRecipe recipe, EmiStack output, PowerType<?> powerType, @Nullable MutableText multiplePowerTypeName) {
        super(padShapedIngredients(recipe), output, EmiStack.of(recipe.getOutput()), recipe.getId(), powerType, multiplePowerTypeName, false);

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

    private static List<EmiIngredient> padShapedIngredients(ShapedRecipe recipe) {
        List<EmiIngredient> list = Lists.newArrayList();
        int i = 0;

        for(int y = 0; y < 3; ++y) {
            for(int x = 0; x < 3; ++x) {
                if (x < recipe.getWidth() && y < recipe.getHeight()) {
                    list.add(EmiIngredient.of(recipe.getIngredients().get(i++)));
                } else {
                    list.add(EmiStack.EMPTY);
                }
            }
        }

        return list;
    }
}
