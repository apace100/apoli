package io.github.apace100.apoli.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyCraftingPower extends ValueModifyingPower {

    private final Identifier recipeIdentifier;
    private final Predicate<ItemStack> itemCondition;

    private final ItemStack newStack;
    private final Consumer<Pair<World, ItemStack>> itemAction;
    private final Consumer<Entity> entityAction;
    private final Consumer<Triple<World, BlockPos, Direction>> blockAction;

    public ModifyCraftingPower(PowerType<?> type, LivingEntity entity, Identifier recipeIdentifier, Predicate<ItemStack> itemCondition, ItemStack newStack, Consumer<Pair<World, ItemStack>> itemAction, Consumer<Entity> entityAction, Consumer<Triple<World, BlockPos, Direction>> blockAction) {
        super(type, entity);
        this.recipeIdentifier = recipeIdentifier;
        this.itemCondition = itemCondition;
        this.newStack = newStack;
        this.itemAction = itemAction;
        this.entityAction = entityAction;
        this.blockAction = blockAction;
    }

    public boolean doesApply(CraftingInventory inventory, Recipe<CraftingInventory> recipe) {
        if(recipeIdentifier != null) {
            if(!recipe.getId().equals(recipeIdentifier)) {
                return false;
            }
        }
        if(itemCondition != null) {
            if(!itemCondition.test(recipe.craft(inventory))) {
                return false;
            }
        }
        return true;
    }

    public ItemStack getNewResult(CraftingInventory inventory, Recipe<CraftingInventory> recipe) {
        ItemStack stack;
        if(newStack != null) {
            stack = newStack.copy();
        } else {
            stack = recipe.craft(inventory);
        }
        if(itemAction != null) {
            itemAction.accept(new Pair<>(entity.world, stack));
        }
        return stack;
    }

    public void executeActions(BlockPos craftingBlock) {
        if(craftingBlock != null && blockAction != null) {
            blockAction.accept(Triple.of(entity.world, craftingBlock, Direction.UP));
        }
        if(entityAction != null) {
            entityAction.accept(entity);
        }
    }
}
