package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class SmeltableCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {
        World world = worldAndStack.getLeft();
        return world != null && world.getRecipeManager()
            .getFirstMatch(RecipeType.SMELTING, new SimpleInventory(worldAndStack.getRight()), world)
            .isPresent();
    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("smeltable"),
            new SerializableData(),
            SmeltableCondition::condition
        );
    }

}
