package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnItemUsePower extends Power {

    private final Predicate<ItemStack> itemCondition;
    private final Consumer<Entity> entityAction;
    private final Consumer<ItemStack> itemAction;

    public ActionOnItemUsePower(PowerType<?> type, LivingEntity entity, Predicate<ItemStack> itemCondition, Consumer<Entity> entityAction, Consumer<ItemStack> itemAction) {
        super(type, entity);
        this.itemCondition = itemCondition;
        this.entityAction = entityAction;
        this.itemAction = itemAction;
    }

    public boolean doesApply(ItemStack stack) {
        return itemCondition == null || itemCondition.test(stack);
    }

    public void executeActions(ItemStack stack) {
        if(itemAction != null) {
            itemAction.accept(stack);
        }
        if(entityAction != null) {
            entityAction.accept(entity);
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("action_on_item_use"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null),
            data ->
                (type, player) -> new ActionOnItemUsePower(type, player,
                    (ConditionFactory<ItemStack>.Instance)data.get("item_condition"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action"),
                    (ActionFactory<ItemStack>.Instance)data.get("item_action")))
            .allowCondition();
    }
}
