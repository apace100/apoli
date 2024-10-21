package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.PowerUtil;
import io.github.apace100.apoli.util.ResourceOperation;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class ChangeResourceEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ChangeResourceEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("resource", ApoliDataTypes.RESOURCE_REFERENCE)
            .add("operation", ApoliDataTypes.RESOURCE_OPERATION)
            .add("change", SerializableDataTypes.INT),
        data -> new ChangeResourceEntityActionType(
            data.get("resource"),
            data.get("operation"),
            data.get("change")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("resource", actionType.resource)
            .set("operation", actionType.operation)
            .set("change", actionType.change)
    );

    private final PowerReference resource;

    private final ResourceOperation operation;
    private final int change;

    public ChangeResourceEntityActionType(PowerReference resource, ResourceOperation operation, int change) {
        this.resource = resource;
        this.operation = operation;
        this.change = change;
    }

    @Override
    protected void execute(Entity entity) {

        PowerType powerType = resource.getType(entity);
        boolean modified = switch (operation) {
            case ADD ->
                PowerUtil.changeResourceValue(powerType, change);
            case SET ->
                PowerUtil.setResourceValue(powerType, change);
        };

        if (modified) {
            PowerHolderComponent.syncPower(entity, resource);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.CHANGE_RESOURCE;
    }

}
