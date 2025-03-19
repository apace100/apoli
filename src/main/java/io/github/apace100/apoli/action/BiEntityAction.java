package io.github.apace100.apoli.action;

import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.action.type.bientity.meta.SequenceBiEntityActionType;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.entity.Entity;

public final class BiEntityAction extends Action<BiEntityActionContext, BiEntityActionType> {

	public static final SerializableDataType<BiEntityAction> DATA_TYPE = SerializableDataType.lazy(() -> ApoliDataTypes.actions("type", BiEntityActionTypes.DATA_TYPE, SequenceBiEntityActionType::new, BiEntityAction::new));

	public BiEntityAction(BiEntityActionType actionType) {
		super(actionType);
	}

	public void execute(Entity actor, Entity target) {
		accept(new BiEntityActionContext(actor, target));
	}

}
