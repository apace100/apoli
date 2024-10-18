package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class FeedEntityActionType extends EntityActionType {

    public static final DataObjectFactory<FeedEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("nutrition", SerializableDataTypes.INT)
            .add("saturation", SerializableDataTypes.FLOAT),
        data -> new FeedEntityActionType(
            data.get("nutrition"),
            data.get("saturation")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("nutrition", actionType.nutrition)
            .set("saturation", actionType.saturation)
    );

    private final int nutrition;
    private final float saturation;

    public FeedEntityActionType(int nutrition, float saturation) {
        this.nutrition = nutrition;
        this.saturation = saturation;
    }

    @Override
    protected void execute(Entity entity) {

        if (entity instanceof PlayerEntity player) {
            player.getHungerManager().add(nutrition, saturation);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.FEED;
    }

}
