package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

//  TODO: Maybe remove in favor of using the `ingredient` item condition type? -eggohito
@Deprecated(forRemoval = true)
public class MeatCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {
        return worldAndStack.getRight().isIn(ItemTags.MEAT);
    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("meat"),
            new SerializableData(),
            MeatCondition::condition
        );
    }

}
