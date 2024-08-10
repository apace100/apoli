package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.PriorityPhase;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnItemUsePowerType extends PowerType implements Prioritized<ActionOnItemUsePowerType> {

    private final Predicate<Pair<World, ItemStack>> itemCondition;
    private final Consumer<Entity> entityAction;
    private final Consumer<Pair<World, StackReference>> itemAction;
    private final TriggerType triggerType;
    private final int priority;

    public ActionOnItemUsePowerType(Power power, LivingEntity entity, Consumer<Entity> entityAction, Consumer<Pair<World, StackReference>> itemAction, Predicate<Pair<World, ItemStack>> itemCondition, TriggerType triggerType, int priority) {
        super(power, entity);
        this.itemCondition = itemCondition;
        this.entityAction = entityAction;
        this.itemAction = itemAction;
        this.triggerType = triggerType;
        this.priority = priority;
    }

    public boolean doesApply(ItemStack stack, TriggerType triggerType, io.github.apace100.apoli.util.PriorityPhase priorityPhase) {
        return this.triggerType == triggerType
            && priorityPhase.test(this.getPriority())
            && (itemCondition == null || itemCondition.test(new Pair<>(entity.getWorld(), stack)));
    }

    public void executeActions(StackReference stack) {

        if (itemAction != null) {
            itemAction.accept(new Pair<>(entity.getWorld(), stack));
        }

        if (entityAction != null) {
            entityAction.accept(entity);
        }

    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    public enum TriggerType {
        INSTANT, START, STOP, FINISH, DURING
    }

    public static void executeActions(Entity user, StackReference useStack, ItemStack checkStack, TriggerType triggerType, PriorityPhase phase) {

        if (user.getWorld().isClient) {
            return;
        }

        ActionOnItemUsePowerType.CallInstance<ActionOnItemUsePowerType> aoiupci = new ActionOnItemUsePowerType.CallInstance<>();
        aoiupci.add(user, ActionOnItemUsePowerType.class, p -> p.doesApply(checkStack, triggerType, phase));

        for (int i = aoiupci.getMaxPriority(); i >= aoiupci.getMinPriority(); i--) {
            aoiupci.forEach(i, aoiup -> aoiup.executeActions(useStack));
        }

    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("action_on_item_use"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("trigger", SerializableDataType.enumValue(TriggerType.class), TriggerType.FINISH)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (power, entity) -> new ActionOnItemUsePowerType(power, entity,
                data.get("entity_action"),
                data.get("item_action"),
                data.get("item_condition"),
                data.get("trigger"),
                data.get("priority")
            )
        ).allowCondition();
    }
}
