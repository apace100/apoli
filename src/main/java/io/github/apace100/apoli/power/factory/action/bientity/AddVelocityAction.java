package io.github.apace100.apoli.power.factory.action.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Vector3f;

import java.util.function.BiFunction;

public class AddVelocityAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> entities) {

        Entity actor = entities.getLeft();
        Entity target = entities.getRight();

        if ((actor == null || target == null) || (target instanceof PlayerEntity && (target.getWorld().isClient ? !data.getBoolean("client") : !data.getBoolean("server")))) {
            return;
        }

        Vector3f vec = new Vector3f(data.getFloat("x"), data.getFloat("y"), data.getFloat("z"));
        TriConsumer<Float, Float, Float> method = data.getBoolean("set") ? target::setVelocity : target::addVelocity;

        Reference reference = data.get("reference");
        Vec3d refVec = reference.apply(actor, target);

        Space.transformVectorToBase(refVec, vec, actor.getYaw(), true); // vector normalized by method
        method.accept(vec.x, vec.y, vec.z);

        target.velocityModified = true;

    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("add_velocity"),
            new SerializableData()
                .add("x", SerializableDataTypes.FLOAT, 0F)
                .add("y", SerializableDataTypes.FLOAT, 0F)
                .add("z", SerializableDataTypes.FLOAT, 0F)
                .add("client", SerializableDataTypes.BOOLEAN, true)
                .add("server", SerializableDataTypes.BOOLEAN, true)
                .add("set", SerializableDataTypes.BOOLEAN, false)
                .add("reference", SerializableDataType.enumValue(Reference.class), Reference.POSITION),
            AddVelocityAction::action
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
