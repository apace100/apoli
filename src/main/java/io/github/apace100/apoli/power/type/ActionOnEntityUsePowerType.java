package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
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

public class ActionOnEntityUsePowerType extends ActiveInteractionPowerType {

    private final Consumer<Pair<Entity, Entity>> biEntityAction;
    private final Predicate<Pair<Entity, Entity>> bientityCondition;

    public ActionOnEntityUsePowerType(Power power, LivingEntity entity, Consumer<Pair<Entity, Entity>> biEntityAction, Predicate<Pair<Entity, Entity>> bientityCondition, Predicate<Pair<World, ItemStack>> itemCondition, EnumSet<Hand> hands, ItemStack resultStack, Consumer<Pair<World, StackReference>> heldItemAction, Consumer<Pair<World, StackReference>> resultItemAction, ActionResult actionResult, int priority) {
        super(power, entity, hands, actionResult, itemCondition, heldItemAction, resultStack, resultItemAction, priority);
        this.biEntityAction = biEntityAction;
        this.bientityCondition = bientityCondition;
    }

    public boolean shouldExecute(Entity other, Hand hand, ItemStack heldStack, PriorityPhase priorityPhase) {
        return priorityPhase.test(this.getPriority())
            && super.shouldExecute(hand, heldStack)
            && (bientityCondition == null || bientityCondition.test(new Pair<>(entity, other)));
    }

    public ActionResult executeAction(Entity other, Hand hand) {

        if (biEntityAction != null) {
            biEntityAction.accept(new Pair<>(entity, other));
        }

        this.performActorItemStuff(this, (PlayerEntity) entity, hand);
        return this.getActionResult();

    }

    public static PowerTypeFactory<ActionOnEntityUsePowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("action_on_entity_use"),
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
            data -> (power, entity) -> new ActionOnEntityUsePowerType(power, entity,
                data.get("bientity_action"),
                data.get("bientity_condition"),
                data.get("item_condition"),
                data.get("hands"),
                data.get("result_stack"),
                data.get("held_item_action"),
                data.get("result_item_action"),
                data.get("action_result"),
                data.get("priority")
            )
        ).allowCondition();
    }
}

