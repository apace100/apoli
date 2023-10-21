package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.IdentifierAlias;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class DamageableCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {
        return worldAndStack.getRight().isDamageable();
    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
        IdentifierAlias.addPathAlias("is_damageable", "damageable");
        return new ConditionFactory<>(
            Apoli.identifier("damageable"),
            new SerializableData(),
            DamageableCondition::condition
        );
    }

}
