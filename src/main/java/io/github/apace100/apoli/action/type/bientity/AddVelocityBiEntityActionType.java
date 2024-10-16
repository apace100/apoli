package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.function.TriConsumer;
import org.joml.Vector3f;

import java.util.function.BiFunction;

public class AddVelocityBiEntityActionType extends BiEntityActionType {

    public static final DataObjectFactory<AddVelocityBiEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("reference", SerializableDataType.enumValue(Reference.class), Reference.POSITION)
            .add("x", SerializableDataTypes.FLOAT, 0F)
            .add("y", SerializableDataTypes.FLOAT, 0F)
            .add("z", SerializableDataTypes.FLOAT, 0F)
            .add("set", SerializableDataTypes.BOOLEAN, false),
        data -> new AddVelocityBiEntityActionType(
            data.get("reference"),
            new Vector3f(
                data.get("x"),
                data.get("y"),
                data.get("z")
            ),
            data.get("set")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("reference", actionType.reference)
            .set("x", actionType.velocity.x())
            .set("y", actionType.velocity.y())
            .set("z", actionType.velocity.z())
            .set("set", actionType.set)
    );

    private final Reference reference;
    private final Vector3f velocity;

    private final boolean set;

    public AddVelocityBiEntityActionType(Reference reference, Vector3f velocity, boolean set) {
        this.reference = reference;
        this.velocity = velocity;
        this.set = set;
    }

    @Override
	protected void execute(Entity actor, Entity target) {

        if (actor == null || target == null) {
            return;
        }

        TriConsumer<Float, Float, Float> method = set
            ? target::setVelocity
            : target::addVelocity;

        Vec3d referenceVec = reference.apply(actor, target);
        Space.transformVectorToBase(referenceVec, velocity, actor.getYaw(), true);  //  Vector normalized by method

        method.accept(velocity.x(), velocity.y(), velocity.z());
        target.velocityModified = true;

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BiEntityActionTypes.ADD_VELOCITY;
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
