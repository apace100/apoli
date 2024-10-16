package io.github.apace100.apoli.action;

import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.inventory.StackReference;
import net.minecraft.world.World;

public class ItemAction extends AbstractAction<ItemActionContext, ItemActionType> {

	public static final SerializableDataType<ItemAction> DATA_TYPE = SerializableDataType.lazy(() -> ApoliDataTypes.action("type", ItemActionTypes.DATA_TYPE, ItemAction::new));

	public ItemAction(ItemActionType actionType) {
		super(actionType);
	}

	public void execute(World world, StackReference stackReference) {
		accept(new ItemActionContext(world, stackReference));
	}

}
