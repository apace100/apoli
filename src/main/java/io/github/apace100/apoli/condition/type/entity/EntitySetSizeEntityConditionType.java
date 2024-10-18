package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.EntitySetPowerType;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;

public class EntitySetSizeEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<EntitySetSizeEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("set", ApoliDataTypes.POWER_REFERENCE)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
        data -> new EntitySetSizeEntityConditionType(
            data.get("set"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("set", conditionType.set)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final PowerReference set;

    private final Comparison comparison;
    private final int compareTo;

    public EntitySetSizeEntityConditionType(PowerReference set, Comparison comparison, int compareTo) {
        this.set = set;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(Entity entity) {

        if (set.getType(entity) instanceof EntitySetPowerType entitySet) {
            return comparison.compare(entitySet.size(), compareTo);
        }

        else {
            return false;
        }

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.ENTITY_SET_SIZE;
    }

}
