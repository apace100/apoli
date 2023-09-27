package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.item.SmeltableCondition;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;

public class ItemConditionsServer {

    public static void register() {
        register(SmeltableCondition.getFactory(() -> Apoli.server.getOverworld()));
    }

    private static void register(ConditionFactory<ItemStack> conditionFactory) {
        Registry.register(ApoliRegistries.ITEM_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
