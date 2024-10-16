package io.github.apace100.apoli.action.type.bientity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;

public class InvertBiEntityActionType extends BiEntityActionType {

    public static final DataObjectFactory<InvertBiEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("action", BiEntityAction.DATA_TYPE),
        data -> new InvertBiEntityActionType(
            data.get("action")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("action", actionType.action)
    );

    private final BiEntityAction action;

    public InvertBiEntityActionType(BiEntityAction action) {
        this.action = action;
    }

    @Override
	protected void execute(Entity actor, Entity target) {
        action.execute(target, actor);
    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BiEntityActionTypes.INVERT;
    }

}
