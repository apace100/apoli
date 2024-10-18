package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import org.apache.commons.lang3.function.TriConsumer;
import org.joml.Vector3f;

public class AddVelocityEntityActionType extends EntityActionType {

    public static final DataObjectFactory<AddVelocityEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("x", SerializableDataTypes.FLOAT, 0F)
            .add("y", SerializableDataTypes.FLOAT, 0F)
            .add("z", SerializableDataTypes.FLOAT, 0F)
            .add("space", ApoliDataTypes.SPACE, Space.WORLD)
            .add("set", SerializableDataTypes.BOOLEAN, false),
        data -> new AddVelocityEntityActionType(
            new Vector3f(
                data.get("x"),
                data.get("y"),
                data.get("z")
            ),
            data.get("space"),
            data.get("set")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("x", actionType.velocity.x())
            .set("y", actionType.velocity.y())
            .set("z", actionType.velocity.z())
            .set("space", actionType.space)
            .set("set", actionType.set)
    );

    private final Vector3f velocity;
    private final Space space;

    private final boolean set;

    public AddVelocityEntityActionType(Vector3f velocity, Space space, boolean set) {
        this.velocity = velocity;
        this.space = space;
        this.set = set;
    }

    @Override
    protected void execute(Entity entity) {

        TriConsumer<Float, Float, Float> method = set
            ? entity::setVelocity
            : entity::addVelocity;

        space.toGlobal(velocity, entity);
        method.accept(velocity.x(), velocity.y(), velocity.z());

        entity.velocityModified = true;

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.ADD_VELOCITY;
    }

}
