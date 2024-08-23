package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import org.apache.commons.lang3.function.TriConsumer;
import org.joml.Vector3f;

public class AddVelocityActionType {

    public static void action(Entity entity, Vector3f velocity, Space space, boolean set) {

        TriConsumer<Float, Float, Float> method = set
            ? entity::setVelocity
            : entity::addVelocity;

        space.toGlobal(velocity, entity);
        method.accept(velocity.x(), velocity.y(), velocity.z());

        entity.velocityModified = true;

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("add_velocity"),
            new SerializableData()
                .add("x", SerializableDataTypes.FLOAT, 0.0F)
                .add("y", SerializableDataTypes.FLOAT, 0.0F)
                .add("z", SerializableDataTypes.FLOAT, 0.0F)
                .add("space", ApoliDataTypes.SPACE, Space.WORLD)
                .add("set", SerializableDataTypes.BOOLEAN, false),
            (data, entity) -> action(entity,
                new Vector3f(data.get("x"), data.get("y"), data.get("z")),
                data.get("space"),
                data.get("set")
            )
        );
    }

}
