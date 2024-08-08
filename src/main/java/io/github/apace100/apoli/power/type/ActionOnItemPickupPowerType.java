package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.mixin.ItemEntityAccessor;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnItemPickupPowerType extends PowerType implements Prioritized<ActionOnItemPickupPowerType> {

    private final Consumer<Pair<Entity, Entity>> biEntityAction;
    private final Consumer<Pair<World, StackReference>> itemAction;

    private final Predicate<Pair<Entity, Entity>> biEntityCondition;
    private final Predicate<Pair<World, ItemStack>> itemCondition;

    private final int priority;

    public ActionOnItemPickupPowerType(Power power, LivingEntity entity, Consumer<Pair<Entity, Entity>> biEntityAction, Predicate<Pair<Entity, Entity>> biEntityCondition, Consumer<Pair<World, StackReference>> itemAction, Predicate<Pair<World, ItemStack>> itemCondition, int priority) {
        super(power, entity);
        this.biEntityAction = biEntityAction;
        this.itemAction = itemAction;
        this.biEntityCondition = biEntityCondition;
        this.itemCondition = itemCondition;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean doesApply(ItemStack stack, Entity thrower) {
        return (itemCondition == null || itemCondition.test(new Pair<>(entity.getWorld(), stack)))
            && (biEntityCondition == null || biEntityCondition.test(new Pair<>(thrower, entity)));
    }

    public void executeActions(ItemStack stack, Entity thrower) {
        if (itemAction != null) itemAction.accept(new Pair<>(entity.getWorld(), InventoryUtil.getStackReferenceFromStack(entity, stack)));
        if (biEntityAction != null) biEntityAction.accept(new Pair<>(thrower, entity));
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

    public static PowerTypeFactory<ActionOnItemPickupPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("action_on_item_pickup"),
            new SerializableData()
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (power, entity) -> new ActionOnItemPickupPowerType(power, entity,
                data.get("bientity_action"),
                data.get("bientity_condition"),
                data.get("item_action"),
                data.get("item_condition"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
