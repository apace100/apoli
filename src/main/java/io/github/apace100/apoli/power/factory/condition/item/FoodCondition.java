package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.PotentiallyEdibleItemStack;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.item.ItemStack;

public class FoodCondition {

    public static boolean condition(SerializableData.Instance data, ItemStack stack) {
        return ((PotentiallyEdibleItemStack) stack)
            .apoli$getFoodComponent()
            .isPresent();
    }

    public static ConditionFactory<ItemStack> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("food"),
            new SerializableData(),
            FoodCondition::condition
        );
    }

}
