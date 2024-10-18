package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.util.PowerUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;

public class SetResourceEntityActionType extends EntityActionType {

    public static final DataObjectFactory<SetResourceEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("resource", ApoliDataTypes.RESOURCE_REFERENCE)
            .add("value", SerializableDataTypes.INT),
        data -> new SetResourceEntityActionType(
            data.get("resource"),
            data.get("value")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("resource", actionType.resource)
            .set("value", actionType.value)
    );

    private final PowerReference resource;
    private final int value;

    public SetResourceEntityActionType(PowerReference resource, int value) {
        this.resource = resource;
        this.value = value;
    }

    @Override
    protected void execute(Entity entity) {

        if (PowerUtil.setResourceValue(resource.getType(entity), value)) {
            PowerHolderComponent.syncPower(entity, resource);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.SET_RESOURCE;
    }

}
