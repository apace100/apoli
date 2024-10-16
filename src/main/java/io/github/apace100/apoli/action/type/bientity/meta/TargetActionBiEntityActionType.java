package io.github.apace100.apoli.action.type.bientity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;

public class TargetActionBiEntityActionType extends BiEntityActionType {

    public static final DataObjectFactory<TargetActionBiEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("action", EntityAction.DATA_TYPE),
        data -> new TargetActionBiEntityActionType(
            data.get("action")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("action", actionType.action)
    );

    private final EntityAction action;

    public TargetActionBiEntityActionType(EntityAction action) {
        this.action = action;
    }

    @Override
	protected void execute(Entity actor, Entity target) {

        if (target != null) {
            action.execute(target);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BiEntityActionTypes.TARGET_ACTION;
    }

}
