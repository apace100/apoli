package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

public class AmountConditionType {

    public static boolean condition(float amount, Comparison comparison, float compareTo) {
        return comparison.compare(amount, compareTo);
    }

    public static ConditionTypeFactory<Pair<DamageSource, Float>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("amount"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.FLOAT),
            (data, sourceAndAmount) -> condition(sourceAndAmount.getRight(),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
