package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class SmeltableConditionType {

    public static boolean condition(World world, ItemStack stack) {
        return world.getRecipeManager()
            .getFirstMatch(RecipeType.SMELTING, new SingleStackRecipeInput(stack), world)
            .isPresent();
    }

    public static ConditionTypeFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("smeltable"),
            new SerializableData(),
            (data, worldAndStack) -> condition(worldAndStack.getLeft(), worldAndStack.getRight())
        );
    }

}
