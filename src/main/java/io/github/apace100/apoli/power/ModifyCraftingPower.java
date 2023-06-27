package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyCraftingPower extends ValueModifyingPower {

    private final Identifier recipeIdentifier;
    private final Predicate<ItemStack> itemCondition;

    private final ItemStack newStack;
    private final Consumer<Pair<World, ItemStack>> itemAction;
    private final Consumer<Pair<World, ItemStack>> lateItemAction;
    private final Consumer<Entity> entityAction;
    private final Consumer<Triple<World, BlockPos, Direction>> blockAction;

    public ModifyCraftingPower(PowerType<?> type, LivingEntity entity, Identifier recipeIdentifier, Predicate<ItemStack> itemCondition, ItemStack newStack, Consumer<Pair<World, ItemStack>> itemAction, Consumer<Pair<World, ItemStack>> lateItemAction, Consumer<Entity> entityAction, Consumer<Triple<World, BlockPos, Direction>> blockAction) {
        super(type, entity);
        this.recipeIdentifier = recipeIdentifier;
        this.itemCondition = itemCondition;
        this.newStack = newStack;
        this.itemAction = itemAction;
        this.lateItemAction = lateItemAction;
        this.entityAction = entityAction;
        this.blockAction = blockAction;
    }

    public boolean doesApply(CraftingInventory inventory, CraftingRecipe recipe) {
        if(recipeIdentifier != null) {
            if(!recipe.getId().equals(recipeIdentifier)) {
                return false;
            }
        }
        if(itemCondition != null) {
            if(!itemCondition.test(recipe.craft(inventory, entity.getWorld().getRegistryManager()))) {
                return false;
            }
        }
        return true;
    }

    public void applyAfterCraftingItemAction(ItemStack output) {
        if(lateItemAction == null) {
            return;
        }
        lateItemAction.accept(new Pair<>(entity.getWorld(), output));
    }

    public ItemStack getNewResult(CraftingInventory inventory, CraftingRecipe recipe) {
        ItemStack stack;
        if(newStack != null) {
            stack = newStack.copy();
        } else {
            stack = recipe.craft(inventory, entity.getWorld().getRegistryManager());
        }
        if(itemAction != null) {
            itemAction.accept(new Pair<>(entity.getWorld(), stack));
        }
        return stack;
    }

    public void executeActions(Optional<BlockPos> craftingBlockPos) {
        if(craftingBlockPos.isPresent() && blockAction != null) {
            blockAction.accept(Triple.of(entity.getWorld(), craftingBlockPos.get(), Direction.UP));
        }
        if(entityAction != null) {
            entityAction.accept(entity);
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_crafting"),
            new SerializableData()
                .add("recipe", SerializableDataTypes.IDENTIFIER, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("result", SerializableDataTypes.ITEM_STACK, null)
                .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("item_action_after_crafting", ApoliDataTypes.ITEM_ACTION, null)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("block_action", ApoliDataTypes.BLOCK_ACTION, null),
            data ->
                (type, player) -> new ModifyCraftingPower(type, player,
                    data.getId("recipe"), data.get("item_condition"),
                    data.get("result"), data.get("item_action"),
                    data.get("item_action_after_crafting"), data.get("entity_action"), data.get("block_action")))
            .allowCondition();
    }
}
