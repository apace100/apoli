package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnItemUsePower extends Power implements Prioritized<ActionOnItemUsePower> {

    private final Predicate<ItemStack> itemCondition;
    private final Consumer<Entity> entityAction;
    private final Consumer<Pair<World, ItemStack>> itemAction;
    private final TriggerType triggerType;
    private final int priority;

    public ActionOnItemUsePower(PowerType<?> type, LivingEntity entity, Predicate<ItemStack> itemCondition, Consumer<Entity> entityAction, Consumer<Pair<World, ItemStack>> itemAction, TriggerType triggerType, int priority) {
        super(type, entity);
        this.itemCondition = itemCondition;
        this.entityAction = entityAction;
        this.itemAction = itemAction;
        this.triggerType = triggerType;
        this.priority = priority;
    }

    public boolean doesApply(ItemStack stack) {
        return itemCondition == null || itemCondition.test(stack);
    }

    public void executeActions(ItemStack stack) {
        if(itemAction != null) {
            itemAction.accept(new Pair<>(entity.getWorld(), stack));
        }
        if(entityAction != null) {
            entityAction.accept(entity);
        }
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    private boolean isInPhase(PriorityPhase phase) {
        return phase == PriorityPhase.ALL || (phase == PriorityPhase.BEFORE && getPriority() >= 0) || (phase == PriorityPhase.AFTER && getPriority() < 0);
    }

    public enum TriggerType {
        INSTANT, START, STOP, FINISH, DURING
    }

    public enum PriorityPhase {
        BEFORE, AFTER, ALL
    }

    public static void executeActions(Entity user, ItemStack useStack, ItemStack checkStack, TriggerType triggerType, PriorityPhase phase) {
        if(user.getWorld().isClient) {
            return;
        }
        ActionOnItemUsePower.CallInstance<ActionOnItemUsePower> callInstance = new ActionOnItemUsePower.CallInstance<>();
        callInstance.add(user, ActionOnItemUsePower.class, p -> p.triggerType == triggerType && p.doesApply(checkStack) && p.isInPhase(phase));
        for(int i = callInstance.getMaxPriority(); i >= callInstance.getMinPriority(); i--) {
            if(!callInstance.hasPowers(i)) {
                continue;
            }
            List<ActionOnItemUsePower> powers = callInstance.getPowers(i);
            for(ActionOnItemUsePower power : powers) {
                power.executeActions(useStack);
            }
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("action_on_item_use"),
            new SerializableData()
                    .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                    .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
                    .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                    .add("trigger", SerializableDataType.enumValue(TriggerType.class), TriggerType.FINISH)
                    .add("priority", SerializableDataTypes.INT, 0),
            data ->
                (type, player) -> new ActionOnItemUsePower(type, player,
                        data.get("item_condition"),
                        data.get("entity_action"),
                        data.get("item_action"),
                        data.get("trigger"),
                        data.getInt("priority")))
            .allowCondition();
    }
}
