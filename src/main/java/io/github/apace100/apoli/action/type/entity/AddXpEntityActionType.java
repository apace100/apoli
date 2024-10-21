package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class AddXpEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<AddXpEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("points", SerializableDataTypes.INT, 0)
            .add("levels", SerializableDataTypes.INT, 0),
        data -> new AddXpEntityActionType(
            data.get("points"),
            data.get("levels")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("points", actionType.points)
            .set("levels", actionType.levels)
    );

    private final int points;
    private final int levels;

    public AddXpEntityActionType(int points, int levels) {
        this.points = points;
        this.levels = levels;
    }

    @Override
    protected void execute(Entity entity) {

        if (!(entity instanceof PlayerEntity player)) {
            return;
        }

        player.addExperience(points);
        player.addExperienceLevels(levels);

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.ADD_XP;
    }

}
