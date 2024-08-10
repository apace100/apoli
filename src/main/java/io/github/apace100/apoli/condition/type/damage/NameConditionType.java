package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

public class NameConditionType {

    public static boolean condition(DamageSource damageSource, String name) {
        return damageSource.getName().equals(name);
    }

    public static ConditionTypeFactory<Pair<DamageSource, Float>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("name"),
            new SerializableData()
                .add("name", SerializableDataTypes.STRING),
            (data, sourceAndAmount) -> condition(sourceAndAmount.getLeft(),
                data.get("name")
            )
        );
    }

}
