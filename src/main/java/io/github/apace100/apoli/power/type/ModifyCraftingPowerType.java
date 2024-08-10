package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.StackReference;
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

public class ModifyCraftingPowerType extends ValueModifyingPowerType implements Prioritized<ModifyCraftingPowerType> {

    public static final Identifier MODIFIED_RESULT_STACK = Apoli.identifier("modified_result_stack");

    private final Identifier recipeIdentifier;
    private final Predicate<Pair<World, ItemStack>> itemCondition;

    private final ItemStack newStack;
    private final Consumer<Pair<World, StackReference>> itemAction;
    private final Consumer<Pair<World, StackReference>> itemActionAfterCrafting;
    private final Consumer<Entity> entityAction;
    private final Consumer<Triple<World, BlockPos, Direction>> blockAction;

    private final int priority;

    public ModifyCraftingPowerType(Power power, LivingEntity entity, Identifier recipeIdentifier, Predicate<Pair<World, ItemStack>> itemCondition, ItemStack newStack, Consumer<Pair<World, StackReference>> itemAction, Consumer<Pair<World, StackReference>> itemActionAfterCrafting, Consumer<Entity> entityAction, Consumer<Triple<World, BlockPos, Direction>> blockAction, int priority) {
        super(power, entity);
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

    public void applyAfterCraftingItemAction(StackReference output) {
        if (itemActionAfterCrafting != null) {
            itemActionAfterCrafting.accept(new Pair<>(entity.getWorld(), output));
        }
    }

    public StackReference getNewResult(StackReference resultStack) {

        if (newStack != null) {
            resultStack.set(newStack.copy());
        }
        if (itemAction != null) {
            itemAction.accept(new Pair<>(entity.getWorld(), resultStack));
        }

        return resultStack;

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

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
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
            data -> (power, entity) -> new ModifyCraftingPowerType(
                power,
                entity,
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
