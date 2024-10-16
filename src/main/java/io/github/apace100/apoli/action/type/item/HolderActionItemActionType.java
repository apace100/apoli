package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.StackReference;
import net.minecraft.world.World;

public class HolderActionItemActionType extends ItemActionType {

    public static final DataObjectFactory<HolderActionItemActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("action", EntityAction.DATA_TYPE),
        data -> new HolderActionItemActionType(
            data.get("action")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("action", actionType.entityAction)
    );

    private final EntityAction entityAction;

    public HolderActionItemActionType(EntityAction entityAction) {
        this.entityAction = entityAction;
    }

    @Override
	protected void execute(World world, StackReference stackReference) {

        Entity holder = ((EntityLinkedItemStack) stackReference.get()).apoli$getEntity(true);

        if (holder != null) {
            entityAction.execute(holder);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return ItemActionTypes.HOLDER;
    }

}
