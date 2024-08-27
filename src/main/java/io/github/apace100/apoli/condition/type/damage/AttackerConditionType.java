package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class AttackerConditionType {

    public static boolean condition(DamageSource damageSource, Predicate<Entity> entityCondition) {
        Entity attacker = damageSource.getAttacker();
        return attacker != null
            && entityCondition.test(attacker);
    }

    public static ConditionTypeFactory<Pair<DamageSource, Float>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("attacker"),
            new SerializableData()
                .add("entity_condition", ApoliDataTypes.ENTITY_CONDITION, null),
            (data, sourceAndAmount) -> condition(sourceAndAmount.getLeft(),
                data.getOrElse("entity_condition", e -> true)
            )
        );
    }

}
