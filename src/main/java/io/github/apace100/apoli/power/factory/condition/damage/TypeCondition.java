package io.github.apace100.apoli.power.factory.condition.damage;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

public class TypeCondition {

    public static boolean condition(SerializableData.Instance data, Pair<DamageSource, Float> damageSourceAndAmount) {
        return damageSourceAndAmount.getLeft().isOf(data.get("damage_type"));
    }

    public static ConditionFactory<Pair<DamageSource, Float>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("type"),
            new SerializableData()
                .add("damage_type", SerializableDataTypes.DAMAGE_TYPE),
            TypeCondition::condition
        );
    }

}
