package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ModifyCraftingPowerType extends PowerType implements Prioritized<ModifyCraftingPowerType> {

    public static final TypedDataObjectFactory<ModifyCraftingPowerType> DATA_FACTORY = createConditionedDataFactory(
        new SerializableData()
            .add("recipe", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("block_action", BlockAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_action_after_crafting", ItemAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("result", SerializableDataTypes.ITEM_STACK.optional(), Optional.empty())
            .addFunctionedDefault("result_stack", SerializableDataTypes.ITEM_STACK.optional(), data -> data.get("result"))
            .add("priority", SerializableDataTypes.INT, 0),
        (data, condition) -> new ModifyCraftingPowerType(
            data.get("recipe"),
            data.get("entity_action"),
            data.get("block_action"),
            data.get("item_action"),
            data.get("item_action_after_crafting"),
            data.get("item_condition"),
            data.get("result_stack"),
            data.get("priority"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("recipe", powerType.recipeId)
            .set("entity_action", powerType.entityAction)
            .set("block_action", powerType.blockAction)
            .set("item_action", powerType.itemAction)
            .set("item_action_after_crafting", powerType.itemActionAfterCrafting)
            .set("item_condition", powerType.itemCondition)
            .set("result_stack", powerType.resultStack)
            .set("priority", powerType.getPriority())
    );

    private final Optional<Identifier> recipeId;

    private final Optional<EntityAction> entityAction;
    private final Optional<BlockAction> blockAction;

    private final Optional<ItemAction> itemAction;
    private final Optional<ItemAction> itemActionAfterCrafting;

    private final Optional<ItemCondition> itemCondition;

    private final Optional<ItemStack> resultStack;
    private final int priority;

    public ModifyCraftingPowerType(Optional<Identifier> recipeId, Optional<EntityAction> entityAction, Optional<BlockAction> blockAction, Optional<ItemAction> itemAction, Optional<ItemAction> itemActionAfterCrafting, Optional<ItemCondition> itemCondition, Optional<ItemStack> resultStack, int priority, Optional<EntityCondition> condition) {
        super(condition);
        this.recipeId = recipeId;
        this.entityAction = entityAction;
        this.blockAction = blockAction;
        this.itemAction = itemAction;
        this.itemActionAfterCrafting = itemActionAfterCrafting;
        this.itemCondition = itemCondition;
        this.resultStack = resultStack;
        this.priority = priority;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_CRAFTING;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean doesApply(Identifier targetRecipeId, ItemStack originalResultStack) {
        return recipeId.map(targetRecipeId::equals).orElse(true)
            && itemCondition.map(condition -> condition.test(getHolder().getWorld(), originalResultStack)).orElse(true);
    }

    public void applyAfterCraftingItemAction(StackReference outputStackReference) {
		itemActionAfterCrafting.ifPresent(action -> action.execute(getHolder().getWorld(), outputStackReference));
    }

    public StackReference getNewResult(StackReference resultStackReference) {

        resultStack
            .map(ItemStack::copy)
            .ifPresent(resultStackReference::set);

        itemAction.ifPresent(action -> action.execute(getHolder().getWorld(), resultStackReference));
        return resultStackReference;

    }

    public void executeActions(Optional<BlockPos> craftingBlockPos) {
        craftingBlockPos.ifPresent(pos -> blockAction.ifPresent(action -> action.execute(getHolder().getWorld(), pos, Optional.empty())));
        entityAction.ifPresent(action -> action.execute(getHolder()));
    }

    public static ItemStack executeAfterCraftingAction(PlayerEntity player, RecipeInputInventory recipeInput, Slot slot, ItemStack stack) {

        if (!(recipeInput instanceof PowerCraftingInventory pci)) {
            return stack;
        }

        StackReference stackReference = InventoryUtil.createStackReference(stack);
        List<ModifyCraftingPowerType> modifyCraftingPowers = pci.apoli$getPowerTypes()
            .stream()
            .filter(ModifyCraftingPowerType.class::isInstance)
            .map(ModifyCraftingPowerType.class::cast)
            .toList();

        if (modifyCraftingPowers.isEmpty()) {
            return stack;
        }

        if (MiscUtil.hasSpaceInInventory(player, stack) && slot instanceof CraftingResultSlot) {

            ItemStack copy = stack.copy();
            modifyCraftingPowers.forEach(mcpt -> mcpt.applyAfterCraftingItemAction(stackReference));

            if (stackReference.get().isEmpty()) {
                slot.onTakeItem(player, copy);
            }

        }

        return stackReference.get();

    }

}
