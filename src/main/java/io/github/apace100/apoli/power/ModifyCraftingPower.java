package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyCraftingPower extends ValueModifyingPower implements Prioritized<ModifyCraftingPower> {

    public static final Identifier MODIFIED_RESULT_STACK = Apoli.identifier("modified_result_stack");

    private final Identifier recipeIdentifier;
    private final Predicate<Pair<World, ItemStack>> itemCondition;

    private final ItemStack newStack;
    private final Consumer<Pair<World, ItemStack>> itemAction;
    private final Consumer<Pair<World, ItemStack>> itemActionAfterCrafting;
    private final Consumer<Entity> entityAction;
    private final Consumer<Triple<World, BlockPos, Direction>> blockAction;

    private final int priority;

    public ModifyCraftingPower(PowerType<?> type, LivingEntity entity, Identifier recipeIdentifier, Predicate<Pair<World, ItemStack>> itemCondition, ItemStack newStack, Consumer<Pair<World, ItemStack>> itemAction, Consumer<Pair<World, ItemStack>> itemActionAfterCrafting, Consumer<Entity> entityAction, Consumer<Triple<World, BlockPos, Direction>> blockAction, int priority) {
        super(type, entity);
        this.recipeIdentifier = recipeIdentifier;
        this.itemCondition = itemCondition;
        this.newStack = newStack;
        this.itemAction = itemAction;
        this.itemActionAfterCrafting = itemActionAfterCrafting;
        this.entityAction = entityAction;
        this.blockAction = blockAction;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean doesApply(Identifier recipeId, ItemStack originalResultStack) {
        return (recipeIdentifier == null || recipeIdentifier.equals(recipeId))
            && (itemCondition == null || itemCondition.test(new Pair<>(entity.getWorld(), originalResultStack)));
    }

    public void applyAfterCraftingItemAction(ItemStack output) {
        if (itemActionAfterCrafting != null) {
            itemActionAfterCrafting.accept(new Pair<>(entity.getWorld(), output));
        }
    }

    public ItemStack getNewResult(ItemStack originalResultStack) {

        ItemStack newResultStack = newStack != null ? newStack.copy() : originalResultStack;
        if (itemAction != null) {
            itemAction.accept(new Pair<>(entity.getWorld(), newResultStack));
        }

        return newResultStack;

    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void executeActions(Optional<BlockPos> craftingBlockPos) {

        if(craftingBlockPos.isPresent() && blockAction != null) {
            blockAction.accept(Triple.of(entity.getWorld(), craftingBlockPos.get(), Direction.UP));
        }

        if(entityAction != null) {
            entityAction.accept(entity);
        }

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("modify_crafting"),
            new SerializableData()
                .add("recipe", SerializableDataTypes.IDENTIFIER, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("result", SerializableDataTypes.ITEM_STACK, null)
                .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("item_action_after_crafting", ApoliDataTypes.ITEM_ACTION, null)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("block_action", ApoliDataTypes.BLOCK_ACTION, null)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (powerType, livingEntity) -> new ModifyCraftingPower(
                powerType,
                livingEntity,
                data.getId("recipe"),
                data.get("item_condition"),
                data.get("result"),
                data.get("item_action"),
                data.get("item_action_after_crafting"),
                data.get("entity_action"),
                data.get("block_action"),
                data.get("priority")
            )
        ).allowCondition();
    }
}
