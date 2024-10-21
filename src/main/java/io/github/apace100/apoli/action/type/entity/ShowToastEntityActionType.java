package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.access.CustomToastViewer;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.CustomToastData;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import net.minecraft.entity.Entity;

public class ShowToastEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ShowToastEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        CustomToastData.FACTORY.getSerializableData().copy(),
        data -> new ShowToastEntityActionType(
            CustomToastData.FACTORY.fromData(data)
        ),
        (actionType, serializableData) ->
            CustomToastData.FACTORY.toData(actionType.customToastData, serializableData)
    );

    private final CustomToastData customToastData;

    public ShowToastEntityActionType(CustomToastData customToastData) {
        this.customToastData = customToastData;
    }

    @Override
    protected void execute(Entity entity) {

        if (!entity.getWorld().isClient() && entity instanceof CustomToastViewer viewer) {
            viewer.apoli$showToast(customToastData);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.SHOW_TOAST;
    }

}
