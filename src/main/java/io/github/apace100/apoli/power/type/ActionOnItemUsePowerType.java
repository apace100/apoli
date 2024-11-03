package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.PriorityPhase;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ActionOnItemUsePowerType extends PowerType implements Prioritized<ActionOnItemUsePowerType> {

    public static final TypedDataObjectFactory<ActionOnItemUsePowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("trigger", SerializableDataType.enumValue(TriggerType.class), TriggerType.FINISH)
            .add("priority", SerializableDataTypes.INT, 0),
        (data, condition) -> new ActionOnItemUsePowerType(
            data.get("entity_action"),
            data.get("item_action"),
            data.get("item_condition"),
            data.get("trigger"),
            data.get("priority"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_action", powerType.entityAction)
            .set("item_action", powerType.itemAction)
            .set("item_condition", powerType.itemCondition)
            .set("trigger", powerType.triggerType)
            .set("priority", powerType.getPriority())
    );

    private final Optional<EntityAction> entityAction;
    private final Optional<ItemAction> itemAction;

    private final Optional<ItemCondition> itemCondition;
    private final TriggerType triggerType;

    private final int priority;

    public ActionOnItemUsePowerType(Optional<EntityAction> entityAction, Optional<ItemAction> itemAction, Optional<ItemCondition> itemCondition, TriggerType triggerType, int priority, Optional<EntityCondition> condition) {
        super(condition);
        this.itemCondition = itemCondition;
        this.entityAction = entityAction;
        this.itemAction = itemAction;
        this.triggerType = triggerType;
        this.priority = priority;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.ACTION_ON_ITEM_USE;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    public boolean doesApply(ItemStack stack, TriggerType triggerType, io.github.apace100.apoli.util.PriorityPhase priorityPhase) {
        return this.triggerType == triggerType
            && priorityPhase.test(this.getPriority())
            && itemCondition.map(condition -> condition.test(getHolder().getWorld(), stack)).orElse(true);
    }

    public void executeActions(StackReference stackReference) {
        itemAction.ifPresent(action -> action.execute(getHolder().getWorld(), stackReference));
        entityAction.ifPresent(action -> action.execute(getHolder()));
    }

    public enum TriggerType {
        INSTANT, START, STOP, FINISH, DURING
    }

    public static void executeActions(Entity user, StackReference useStack, ItemStack checkStack, TriggerType triggerType, PriorityPhase phase) {

        if (user.getWorld().isClient()) {
            return;
        }

        ActionOnItemUsePowerType.CallInstance<ActionOnItemUsePowerType> aoiupci = new ActionOnItemUsePowerType.CallInstance<>();
        aoiupci.add(user, ActionOnItemUsePowerType.class, p -> p.doesApply(checkStack, triggerType, phase));

        for (int i = aoiupci.getMaxPriority(); i >= aoiupci.getMinPriority(); i--) {
            aoiupci.forEach(i, aoiup -> aoiup.executeActions(useStack));
        }

    }

}
