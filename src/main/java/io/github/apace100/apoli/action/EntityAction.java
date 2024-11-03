package io.github.apace100.apoli.action;

import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.action.type.entity.meta.AndEntityActionType;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.entity.Entity;

public final class EntityAction extends AbstractAction<EntityActionContext, EntityActionType> {

	public static final SerializableDataType<EntityAction> DATA_TYPE = SerializableDataType.lazy(() -> ApoliDataTypes.actions("type", EntityActionTypes.DATA_TYPE, AndEntityActionType::new, EntityAction::new));

	public EntityAction(EntityActionType actionType) {
		super(actionType);
	}

	public void execute(Entity entity) {
		accept(new EntityActionContext(entity));
	}

}
