package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.type.DamageConditionType;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;

public class AttackerDamageConditionType extends DamageConditionType {

    public static final DataObjectFactory<AttackerDamageConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
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
        this.entityCondition = AbstractCondition.setPowerType(entityCondition, getPowerType());
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
