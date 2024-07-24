package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.power.factory.condition.ItemConditions;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class FireResistantCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {
        return worldAndStack.getRight().contains(DataComponentTypes.FIRE_RESISTANT);
    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {

        var factory = new ConditionFactory<>(
            Apoli.identifier("fire_resistant"),
            new SerializableData(),
            FireResistantCondition::condition
        );

        ItemConditions.ALIASES.addPathAlias("fireproof", factory.getSerializerId().getPath());
        return factory;

    }

}
