package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.power.type.EdibleItemPowerType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class FoodItemConditionType extends ItemConditionType {

    @Override
    public boolean test(World world, ItemStack stack) {
        return EdibleItemPowerType.get(stack).isPresent()
            || stack.contains(DataComponentTypes.FOOD);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return ItemConditionTypes.FOOD;
    }

}
