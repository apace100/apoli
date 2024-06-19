package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.power.factory.condition.DamageConditions;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class DamageableCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {
        return worldAndStack.getRight().isDamageable();
    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {

        ConditionFactory<Pair<World, ItemStack>> factory = new ConditionFactory<>(
            Apoli.identifier("damageable"),
            new SerializableData(),
            DamageableCondition::condition
        );

        DamageConditions.ALIASES.addPathAlias("is_damageable", factory.getSerializerId().getPath());
        return factory;

    }

}
