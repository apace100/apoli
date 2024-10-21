package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.PowerUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class ResourceEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<ResourceEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("resource", ApoliDataTypes.RESOURCE_REFERENCE)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
        data -> new ResourceEntityConditionType(
            data.get("resource"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("resource", conditionType.resource)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final PowerReference resource;

    private final Comparison comparison;
    private final int compareTo;

    public ResourceEntityConditionType(PowerReference resource, Comparison comparison, int compareTo) {
        this.resource = resource;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(Entity entity) {
        return comparison.compare(PowerUtil.getResourceValue(resource.getType(entity)), compareTo);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.RESOURCE;
    }

}
