package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
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

public class PreventBeingUsedPower extends InteractionPower {

    private final Consumer<Pair<Entity, Entity>> biEntityAction;
    private final Predicate<Pair<Entity, Entity>> bientityCondition;

    public PreventBeingUsedPower(PowerType<?> type, LivingEntity entity, EnumSet<Hand> hands, ActionResult actionResult, Predicate<Pair<World, ItemStack>> itemCondition, Consumer<Pair<World, ItemStack>> heldItemAction, ItemStack itemResult, Consumer<Pair<World, ItemStack>> itemAction, Consumer<Pair<Entity, Entity>> biEntityAction, Predicate<Pair<Entity, Entity>> bientityCondition) {
        super(type, entity, hands, actionResult, itemCondition, heldItemAction, itemResult, itemAction);
        this.biEntityAction = biEntityAction;
        this.bientityCondition = bientityCondition;
    }

    public boolean doesApply(PlayerEntity other, Hand hand, ItemStack heldStack) {
        if(!shouldExecute(hand, heldStack)) {
            return false;
        }
        return bientityCondition == null || bientityCondition.test(new Pair<>(other, entity));
    }

    public ActionResult executeAction(PlayerEntity other, Hand hand) {
        if(biEntityAction != null) {
            biEntityAction.accept(new Pair<>(other, entity));
        }
        performActorItemStuff(this, other, hand);
        return getActionResult();
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("prevent_being_used"),
            new SerializableData()
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("hands", SerializableDataTypes.HAND_SET, EnumSet.allOf(Hand.class))
                .add("result_stack", SerializableDataTypes.ITEM_STACK, null)
                .add("held_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("result_item_action", ApoliDataTypes.ITEM_ACTION, null),
            data ->
                (type, player) -> {
                    return new PreventBeingUsedPower(type, player,
                        (EnumSet<Hand>)data.get("hands"),
                        ActionResult.FAIL,
                        (Predicate<Pair<World, ItemStack>>)data.get("item_condition"),
                        (Consumer<Pair<World, ItemStack>>)data.get("held_item_action"),
                        (ItemStack)data.get("result_stack"),
                        (Consumer<Pair<World, ItemStack>>)data.get("result_item_action"),
                        (Consumer<Pair<Entity, Entity>>) data.get("bientity_action"),
                        (ConditionFactory<Pair<Entity, Entity>>.Instance)data.get("bientity_condition"));
                })
            .allowCondition();
    }
}
