package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Vector3f;

import java.util.function.BiFunction;

public class AddVelocityActionType {

    public static void action(Entity actor, Entity target, Reference reference, Vector3f velocity, boolean set) {

        TriConsumer<Float, Float, Float> method = set
            ? target::setVelocity
            : target::addVelocity;

        Vec3d refVec = reference.apply(actor, target);
        Space.transformVectorToBase(refVec, velocity, actor.getYaw(), true); // vector normalized by method

        method.accept(velocity.x, velocity.y, velocity.z);
        target.velocityModified = true;

    }

    public static ActionTypeFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("add_velocity"),
            new SerializableData()
                .add("reference", SerializableDataType.enumValue(Reference.class), Reference.POSITION)
                .add("x", SerializableDataTypes.FLOAT, 0F)
                .add("y", SerializableDataTypes.FLOAT, 0F)
                .add("z", SerializableDataTypes.FLOAT, 0F)
                .add("set", SerializableDataTypes.BOOLEAN, false),
            (data, actorAndTarget) -> action(actorAndTarget.getLeft(), actorAndTarget.getRight(),
                data.get("reference"),
                new Vector3f(data.get("x"), data.get("y"), data.get("z")),
                data.get("set")
            )
        );
    }

    public enum Reference {

        POSITION((actor, target) -> target.getPos().subtract(actor.getPos())),
        ROTATION((actor, target) -> {

            float pitch = actor.getPitch();
            float yaw = actor.getYaw();

            float i = 0.017453292F;

            float j = -MathHelper.sin(yaw * i) * MathHelper.cos(pitch * i);
            float k = -MathHelper.sin(pitch * i);
            float l =  MathHelper.cos(yaw * i) * MathHelper.cos(pitch * i);

            return new Vec3d(j, k, l);

        });

        final BiFunction<Entity, Entity, Vec3d> refFunction;
        Reference(BiFunction<Entity, Entity, Vec3d> refFunction) {
            this.refFunction = refFunction;
        }

        public Vec3d apply(Entity actor, Entity target) {
            return refFunction.apply(actor, target);
        }

    }

}
