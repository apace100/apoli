package io.github.apace100.apoli.action;

import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.entity.Entity;

public class EntityAction extends AbstractAction<EntityActionContext, EntityActionType> {

	public static final SerializableDataType<EntityAction> DATA_TYPE = SerializableDataType.lazy(() -> ApoliDataTypes.action("type", EntityActionTypes.DATA_TYPE, EntityAction::new));

	public EntityAction(EntityActionType actionType) {
		super(actionType);
	}

	public void execute(Entity entity) {
		accept(new EntityActionContext(entity));
	}

}
