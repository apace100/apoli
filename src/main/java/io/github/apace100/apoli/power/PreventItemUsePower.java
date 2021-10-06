package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class PreventItemUsePower extends Power {

    private final Predicate<ItemStack> predicate;

    public PreventItemUsePower(PowerType<?> type, LivingEntity entity, Predicate<ItemStack> predicate) {
        super(type, entity);
        this.predicate = predicate;
    }

    public boolean doesPrevent(ItemStack stack) {
        return predicate.test(stack);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("prevent_item_use"),
            new SerializableData()
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null),
            data ->
                (type, player) ->
                    new PreventItemUsePower(type, player, data.isPresent("item_condition") ? (ConditionFactory<ItemStack>.Instance)data.get("item_condition") : item -> true))
            .allowCondition();
    }
}
