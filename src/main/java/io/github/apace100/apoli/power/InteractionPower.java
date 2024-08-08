package io.github.apace100.apoli.power;

import io.github.apace100.apoli.util.InventoryUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SlotRanges;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class InteractionPower extends Power {

    private final EnumSet<Hand> hands;
    private final ActionResult actionResult;
    private final Predicate<Pair<World, ItemStack>> itemCondition;
    protected final Consumer<Pair<World, StackReference>> heldItemAction;
    protected final ItemStack itemResult;
    protected final Consumer<Pair<World, StackReference>> resultItemAction;

    public InteractionPower(PowerType type, LivingEntity entity, EnumSet<Hand> hands, ActionResult actionResult, Predicate<Pair<World, ItemStack>> itemCondition, Consumer<Pair<World, StackReference>> heldItemAction, ItemStack itemResult, Consumer<Pair<World, StackReference>> resultItemAction) {
        super(type, entity);
        this.hands = hands;
        this.actionResult = actionResult;
        this.itemCondition = itemCondition;
        this.heldItemAction = heldItemAction;
        this.itemResult = itemResult;
        this.resultItemAction = resultItemAction;
    }

    public boolean shouldExecute(Hand hand, ItemStack heldStack) {
        if(!doesApplyToHand(hand)) {
            return false;
        }
        if(!doesApplyToItem(heldStack)) {
            return false;
        }
        return true;
    }

    public boolean doesApplyToHand(Hand hand) {
        return hands.contains(hand);
    }

    public boolean doesApplyToItem(ItemStack heldStack) {
        return itemCondition == null || itemCondition.test(new Pair<>(entity.getWorld(), heldStack));
    }

    public ActionResult getActionResult() {
        return actionResult;
    }

    protected void performActorItemStuff(InteractionPower power, PlayerEntity actor, Hand hand) {
        StackReference heldStack = actor.getStackReference(hand == Hand.OFF_HAND ? SlotRanges.fromName("weapon.offhand").getSlotIds().getFirst() : SlotRanges.fromName("weapon.mainhand").getSlotIds().getFirst());
        if(power.heldItemAction != null) {
            heldItemAction.accept(new Pair<>(actor.getWorld(), heldStack));
        }
        if (power.itemResult != null) {
            heldStack.set(power.itemResult);
        }
        StackReference resultingStack = InventoryUtil.createStackReference(power.itemResult == null ? heldStack.get() : power.itemResult.copy());
        boolean modified = power.itemResult != null;
        if(power.resultItemAction != null) {
            resultItemAction.accept(new Pair<>(actor.getWorld(), heldStack));
            modified = true;
        }
        if(modified) {
            if(heldStack.get().isEmpty()) {
                actor.setStackInHand(hand, resultingStack.get());
            } else {
                actor.getInventory().offerOrDrop(resultingStack.get());
            }
        }
    }
}
