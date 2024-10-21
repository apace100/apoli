package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.type.DamageConditionType;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;

public class AttackerDamageConditionType extends DamageConditionType {

    public static final TypedDataObjectFactory<AttackerDamageConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("entity_condition", EntityCondition.DATA_TYPE),
        data -> new AttackerDamageConditionType(
            data.get("entity_condition")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("entity_condition", conditionType.entityCondition)
    );

    private final EntityCondition entityCondition;

    public AttackerDamageConditionType(EntityCondition entityCondition) {
        this.entityCondition = entityCondition;
    }

    @Override
    public boolean test(DamageSource source, float amount) {
        Entity attacker = source.getAttacker();
        return attacker != null
            && entityCondition.test(attacker);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return DamageConditionTypes.ATTACKER;
    }

}
