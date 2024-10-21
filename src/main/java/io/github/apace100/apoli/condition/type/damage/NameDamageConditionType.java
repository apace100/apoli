package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.DamageConditionType;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.damage.DamageSource;

public class NameDamageConditionType extends DamageConditionType {

    public static final TypedDataObjectFactory<NameDamageConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("name", SerializableDataTypes.STRING),
        data -> new NameDamageConditionType(
            data.get("name")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("name", conditionType.name)
    );

    private final String name;

    public NameDamageConditionType(String name) {
        this.name = name;
    }

    @Override
    public boolean test(DamageSource source, float amount) {
        return source.getName().equals(name);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return DamageConditionTypes.NAME;
    }

}
