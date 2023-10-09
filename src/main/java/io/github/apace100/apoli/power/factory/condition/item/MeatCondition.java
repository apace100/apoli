package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.PotentiallyEdibleItemStack;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class MeatCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> stackAndWorld) {
        return ((PotentiallyEdibleItemStack) stackAndWorld.getRight())
            .apoli$getFoodComponent()
            .map(FoodComponent::isMeat)
            .orElse(false);
    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("meat"),
            new SerializableData(),
            MeatCondition::condition
        );
    }

}
