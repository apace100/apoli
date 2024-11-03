package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.mixin.ItemEntityAccessor;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ActionOnItemPickupPowerType extends PowerType implements Prioritized<ActionOnItemPickupPowerType> {

    public static final TypedDataObjectFactory<ActionOnItemPickupPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("priority", SerializableDataTypes.INT, 0),
        (data, condition) -> new ActionOnItemPickupPowerType(
            data.get("bientity_action"),
            data.get("item_action"),
            data.get("bientity_condition"),
            data.get("item_condition"),
            data.get("priority"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("bientity_action", powerType.biEntityAction)
            .set("item_action", powerType.itemAction)
            .set("bientity_condition", powerType.biEntityCondition)
            .set("item_condition", powerType.itemCondition)
            .set("priority", powerType.getPriority())
    );

    private final Optional<BiEntityAction> biEntityAction;
    private final Optional<ItemAction> itemAction;

    private final Optional<BiEntityCondition> biEntityCondition;
    private final Optional<ItemCondition> itemCondition;

    private final int priority;

    public ActionOnItemPickupPowerType(Optional<BiEntityAction> biEntityAction, Optional<BiEntityCondition> biEntityCondition, Optional<ItemAction> itemAction, Optional<ItemCondition> itemCondition, int priority, Optional<EntityCondition> condition) {
        super(condition);
        this.biEntityAction = biEntityAction;
        this.itemAction = itemAction;
        this.biEntityCondition = biEntityCondition;
        this.itemCondition = itemCondition;
        this.priority = priority;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.ACTION_ON_ITEM_PICKUP;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean doesApply(ItemStack stack, Entity thrower) {
        return itemCondition.map(condition -> condition.test(getHolder().getWorld(), stack)).orElse(true)
            && biEntityCondition.map(condition -> condition.test(thrower, getHolder())).orElse(true);
    }

    public void executeActions(ItemStack stack, Entity thrower) {
        itemAction.ifPresent(action -> action.execute(getHolder().getWorld(), InventoryUtil.getStackReferenceFromStack(getHolder(), stack)));
        biEntityAction.ifPresent(action -> action.execute(thrower, getHolder()));
    }

    public static void executeActions(ItemEntity itemEntity, Entity entity) {

        if (!PowerHolderComponent.KEY.isProvidedBy(entity)) {
            return;
        }

        ItemStack stack = itemEntity.getStack();
        Entity throwerEntity = MiscUtil.getEntityByUuid(((ItemEntityAccessor) itemEntity).getThrower(), entity.getServer());

        CallInstance<ActionOnItemPickupPowerType> aoippci = new CallInstance<>();
        aoippci.add(entity, ActionOnItemPickupPowerType.class, p -> p.doesApply(stack, throwerEntity));

        for (int i = aoippci.getMaxPriority(); i >= aoippci.getMinPriority(); i--) {
            aoippci.forEach(i, p -> p.executeActions(stack, throwerEntity));
        }

    }

}
