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

public class PreventItemPickupPowerType extends PowerType implements Prioritized<PreventItemPickupPowerType> {

    private final Consumer<Pair<Entity, Entity>> biEntityActionThrower;
    private final Consumer<Pair<Entity, Entity>> biEntityActionItem;
    private final Consumer<Pair<World, StackReference>> itemAction;

    private final Predicate<Pair<Entity, Entity>> biEntityCondition;
    private final Predicate<Pair<World, ItemStack>> itemCondition;

    private final int priority;

    public PreventItemPickupPowerType(Power power, LivingEntity entity, Consumer<Pair<Entity, Entity>> biEntityActionThrower, Consumer<Pair<Entity, Entity>> biEntityActionItem, Consumer<Pair<World, StackReference>> itemAction, Predicate<Pair<Entity, Entity>> biEntityCondition, Predicate<Pair<World, ItemStack>> itemCondition, int priority) {
        super(power, entity);
        this.biEntityActionThrower = biEntityActionThrower;
        this.biEntityActionItem = biEntityActionItem;
        this.itemAction = itemAction;
        this.biEntityCondition = biEntityCondition;
        this.itemCondition = itemCondition;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean doesPrevent(ItemStack stack, Entity thrower) {
        return (itemCondition == null || itemCondition.test(new Pair<>(entity.getWorld(), stack)))
            && (biEntityCondition == null || biEntityCondition.test(new Pair<>(thrower, entity)));
    }

    public void executeActions(ItemEntity itemEntity, Entity thrower) {
        if (itemAction != null) {
            StackReference reference = InventoryUtil.createStackReference(itemEntity.getStack());
            itemAction.accept(new Pair<>(entity.getWorld(), reference));
            itemEntity.setStack(reference.get());
        }
        if (biEntityActionThrower != null) {
            biEntityActionThrower.accept(new Pair<>(thrower, entity));
        }
        if (biEntityActionItem != null) {
            biEntityActionItem.accept(new Pair<>(entity, itemEntity));
        }
    }

    public static boolean doesPrevent(ItemEntity itemEntity, Entity entity) {

        if (!PowerHolderComponent.KEY.isProvidedBy(entity)) {
            return false;
        }

        ItemStack stack = itemEntity.getStack();
        Entity throwerEntity = MiscUtil.getEntityByUuid(((ItemEntityAccessor) itemEntity).getThrower(), entity.getServer());

        CallInstance<PreventItemPickupPowerType> pippci = new CallInstance<>();
        pippci.add(entity, PreventItemPickupPowerType.class, p -> p.doesPrevent(stack, throwerEntity));

        boolean prevented = false;
        for (int i = pippci.getMaxPriority(); i >= pippci.getMinPriority(); i--) {

            if (!pippci.hasPowers(i)) {
                continue;
            }

            pippci.forEach(i, p -> p.executeActions(itemEntity, throwerEntity));
            prevented = true;

        }

        return prevented;

    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("prevent_item_pickup"),
            new SerializableData()
                .add("bientity_action_thrower", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("bientity_action_item", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (power, entity) -> new PreventItemPickupPowerType(power, entity,
                data.get("bientity_action_thrower"),
                data.get("bientity_action_item"),
                data.get("item_action"),
                data.get("bientity_condition"),
                data.get("item_condition"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
