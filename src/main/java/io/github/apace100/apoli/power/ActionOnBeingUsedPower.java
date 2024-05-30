package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.PriorityPhase;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnBeingUsedPower extends ActiveInteractionPower {

    private final Consumer<Pair<Entity, Entity>> biEntityAction;
    private final Predicate<Pair<Entity, Entity>> bientityCondition;

    public ActionOnBeingUsedPower(PowerType<?> type, LivingEntity entity, EnumSet<Hand> hands, ActionResult actionResult, Predicate<Pair<World, ItemStack>> itemCondition, Consumer<Pair<World, StackReference>> heldItemAction, ItemStack itemResult, Consumer<Pair<World, StackReference>> itemAction, Consumer<Pair<Entity, Entity>> biEntityAction, Predicate<Pair<Entity, Entity>> bientityCondition, int priority) {
        super(type, entity, hands, actionResult, itemCondition, heldItemAction, itemResult, itemAction, priority);
        this.biEntityAction = biEntityAction;
        this.bientityCondition = bientityCondition;
    }

    public boolean shouldExecute(PlayerEntity other, Hand hand, ItemStack heldStack, PriorityPhase priorityPhase) {
        return priorityPhase.test(this.getPriority())
            && super.shouldExecute(hand, heldStack)
            && (bientityCondition == null || bientityCondition.test(new Pair<>(other, entity)));
    }

    public ActionResult executeAction(PlayerEntity other, Hand hand) {
        if(biEntityAction != null) {
            biEntityAction.accept(new Pair<>(other, entity));
        }
        performActorItemStuff(this, other, hand);
        return getActionResult();
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("action_on_being_used"),
            new SerializableData()
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("hands", SerializableDataTypes.HAND_SET, EnumSet.allOf(Hand.class))
                .add("result_stack", SerializableDataTypes.ITEM_STACK, null)
                .add("held_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("result_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("action_result", SerializableDataTypes.ACTION_RESULT, ActionResult.SUCCESS)
                .add("priority", SerializableDataTypes.INT, 0),
            data ->
                (type, player) -> new ActionOnBeingUsedPower(type, player,
                    data.get("hands"),
                    data.get("action_result"),
                    data.get("item_condition"),
                    data.get("held_item_action"),
                    data.get("result_stack"),
                    data.get("result_item_action"),
                    data.get("bientity_action"),
                    data.get("bientity_condition"),
                    data.get("priority")))
            .allowCondition();
    }
}

