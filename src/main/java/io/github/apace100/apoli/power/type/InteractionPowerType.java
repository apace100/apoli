package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.BiFunction;

public abstract class InteractionPowerType extends PowerType {

    protected final Optional<ItemAction> heldItemAction;
    protected final Optional<ItemCondition> heldItemCondition;

    protected final Optional<ItemAction> resultItemAction;
    protected final Optional<ItemStack> resultStack;

    protected final EnumSet<Hand> hands;
    protected final ActionResult actionResult;

    public InteractionPowerType(Optional<ItemAction> heldItemAction, Optional<ItemCondition> heldItemCondition, Optional<ItemAction> resultItemAction, Optional<ItemStack> resultStack, EnumSet<Hand> hands, ActionResult actionResult, Optional<EntityCondition> condition) {
        super(condition);
        this.hands = hands;
        this.actionResult = actionResult;
        this.heldItemCondition = heldItemCondition;
        this.heldItemAction = heldItemAction;
        this.resultStack = resultStack;
        this.resultItemAction = resultItemAction;
    }

    public InteractionPowerType(Optional<ItemAction> heldItemAction, Optional<ItemCondition> heldItemCondition, Optional<ItemStack> resultStack, Optional<ItemAction> resultItemAction, EnumSet<Hand> hands, ActionResult actionResult) {
        this(heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, actionResult, Optional.empty());
    }

    public boolean shouldExecute(Hand hand, ItemStack heldStack) {
        return doesApplyToHand(hand)
            && doesApplyToItem(heldStack);
    }

    public boolean doesApplyToHand(Hand hand) {
        return hands.contains(hand);
    }

    public boolean doesApplyToItem(ItemStack heldStack) {
        return heldItemCondition
            .map(condition -> condition.test(getHolder().getWorld(), heldStack))
            .orElse(true);
    }

    public ActionResult getActionResult() {
        return actionResult;
    }

    protected void performActorItemStuff(PlayerEntity actor, Hand hand) {

        StackReference heldStackReference = getHeldStackReference(actor, hand);
        heldItemAction.ifPresent(action -> action.execute(actor.getWorld(), heldStackReference));

        ItemStack resultStack = this.resultStack.isPresent()
            ? this.resultStack.get().copy()
            : heldStackReference.get().copy();

        StackReference resultStackReference = InventoryUtil.createStackReference(resultStack);
        boolean modified = this.resultStack.isPresent() || resultItemAction.isPresent();

        resultItemAction.ifPresent(action -> action.execute(actor.getWorld(), resultStackReference));

        if (modified) {

            if (heldStackReference.get().isEmpty()) {
                actor.setStackInHand(hand, resultStackReference.get());
            }

            else {
                actor.getInventory().offerOrDrop(resultStackReference.get());
            }

        }

    }

    protected static StackReference getHeldStackReference(PlayerEntity player, Hand hand) {

        PlayerInventory playerInventory = player.getInventory();
        int selectedSlot = playerInventory.selectedSlot;

        if (hand == Hand.MAIN_HAND && PlayerInventory.isValidHotbarIndex(selectedSlot)) {
            return StackReference.of(playerInventory, selectedSlot);
        }

        else if (hand == Hand.OFF_HAND) {
            return StackReference.of(playerInventory.offHand::getFirst, stack -> playerInventory.offHand.set(0, stack));
        }

        else {
            return StackReference.EMPTY;
        }

    }

    public static <T extends InteractionPowerType> TypedDataObjectFactory<T> createConditionedDataFactory(SerializableData serializableData, FromData<T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
        return PowerType.createConditionedDataFactory(
            serializableData
                .add("held_item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
                .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
                .addFunctionedDefault("held_item_condition", ItemCondition.DATA_TYPE.optional(), data -> data.get("item_condition"))
                .add("result_item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
                .add("result_stack", SerializableDataTypes.ITEM_STACK.optional(), Optional.empty())
                .add("hands", SerializableDataTypes.HAND_SET, EnumSet.allOf(Hand.class))
                .add("action_result", SerializableDataTypes.ACTION_RESULT, ActionResult.SUCCESS),
            (data, condition) -> fromData.apply(
                data,
                data.get("held_item_action"),
                data.get("held_item_condition"),
                data.get("result_item_action"),
                data.get("result_stack"),
                data.get("hands"),
                data.get("action_result"),
                condition
            ),
            (t, _serializableData) -> toData.apply(t, _serializableData)
                .set("held_item_action", t.heldItemAction)
                .set("held_item_condition", t.heldItemCondition)
                .set("result_item_action", t.resultItemAction)
                .set("result_stack", t.resultStack)
                .set("hands", t.hands)
                .set("action_result", t.actionResult)
        );
    }

    @FunctionalInterface
    public interface FromData<T extends InteractionPowerType> {
        T apply(SerializableData.Instance data, Optional<ItemAction> heldItemAction, Optional<ItemCondition> heldItemCondition, Optional<ItemAction> resultItemAction, Optional<ItemStack> resultStack, EnumSet<Hand> hands, ActionResult actionResult, Optional<EntityCondition> condition);
    }

}
