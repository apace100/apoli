package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class PreventItemUsePower extends Power {

    private final Predicate<ItemStack> itemCondition;
    private final boolean includeBlockItems;

    public PreventItemUsePower(PowerType<?> type, LivingEntity entity, Predicate<ItemStack> itemCondition, boolean includeBlockItems) {
        super(type, entity);
        this.itemCondition = itemCondition;
        this.includeBlockItems = includeBlockItems;
    }

    public boolean doesPreventUsage(ItemStack stack) {
        return itemCondition.test(stack);
    }

    public boolean doesPreventPlacement(ItemStack stack) {
        return includeBlockItems && itemCondition.test(stack);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("prevent_item_use"),
            new SerializableData()
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("include_block_items", SerializableDataTypes.BOOLEAN, false),
            data ->
                (type, player) ->
                    new PreventItemUsePower(
                        type,
                        player,
                        data.isPresent("item_condition") ? data.get("item_condition") : item -> true,
                        data.getBoolean("include_block_items")
                    ))
            .allowCondition();
    }
}
