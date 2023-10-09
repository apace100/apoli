package io.github.apace100.apoli.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
    protected final Consumer<Pair<World, ItemStack>> heldItemAction;
    protected final ItemStack itemResult;
    protected final Consumer<Pair<World, ItemStack>> resultItemAction;

    public InteractionPower(PowerType<?> type, LivingEntity entity, EnumSet<Hand> hands, ActionResult actionResult, Predicate<Pair<World, ItemStack>> itemCondition, Consumer<Pair<World, ItemStack>> heldItemAction, ItemStack itemResult, Consumer<Pair<World, ItemStack>> resultItemAction) {
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
        ItemStack heldStack = actor.getStackInHand(hand);
        if(power.heldItemAction != null) {
            power.heldItemAction.accept(new Pair<>(actor.getWorld(), heldStack));
        }
        ItemStack resultingStack = power.itemResult == null ? heldStack : power.itemResult.copy();
        boolean modified = power.itemResult != null;
        if(power.resultItemAction != null) {
            power.resultItemAction.accept(new Pair<>(actor.getWorld(), resultingStack));
            modified = true;
        }
        if(modified) {
            if(heldStack.isEmpty()) {
                actor.setStackInHand(hand, resultingStack);
            } else {
                actor.getInventory().offerOrDrop(resultingStack);
            }
        }
    }
}
