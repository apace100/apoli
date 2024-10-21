package io.github.apace100.apoli.action.type.bientity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class ActorActionBiEntityActionType extends BiEntityActionType {

    public static final TypedDataObjectFactory<ActorActionBiEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("action", EntityAction.DATA_TYPE),
        data -> new ActorActionBiEntityActionType(
            data.get("action")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("action", actionType.action)
    );

    private final EntityAction action;

    public ActorActionBiEntityActionType(EntityAction action) {
        this.action = action;
    }

    @Override
	protected void execute(Entity actor, Entity target) {

        if (actor != null) {
            action.execute(actor);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BiEntityActionTypes.ACTOR_ACTION;
    }

}
