package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

import java.util.Optional;

public class BlockActionAtEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<BlockActionAtEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("block_action", BlockAction.DATA_TYPE),
        data -> new BlockActionAtEntityActionType(
            data.get("block_action")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("block_action", actionType.blockAction)
    );

    private final BlockAction blockAction;

    public BlockActionAtEntityActionType(BlockAction blockAction) {
        this.blockAction = blockAction;
    }

    @Override
    protected void execute(Entity entity) {
        blockAction.execute(entity.getWorld(), entity.getBlockPos(), Optional.empty());
    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.BLOCK_ACTION_AT;
    }

}
