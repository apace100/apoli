package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.util.PowerUtil;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

import java.util.List;

public class ModifyResourceEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ModifyResourceEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("resource", ApoliDataTypes.RESOURCE_REFERENCE)
            .add("modifier", Modifier.DATA_TYPE),
        data -> new ModifyResourceEntityActionType(
            data.get("resource"),
            data.get("modifier")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("resource", actionType.resource)
            .set("modifier", actionType.modifier)
    );

    private final PowerReference resource;
    private final Modifier modifier;

    public ModifyResourceEntityActionType(PowerReference resource, Modifier modifier) {
        this.resource = resource;
        this.modifier = modifier;
    }

    @Override
    protected void execute(Entity entity) {

        if (PowerUtil.modifyResourceValue(resource.getType(entity), List.of(modifier))) {
            PowerHolderComponent.syncPower(entity, resource);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.MODIFY_RESOURCE;
    }

}
