package io.github.apace100.apoli.condition.type.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.function.Function;

public class RelativeRotationBiEntityConditionType extends BiEntityConditionType {

    public static final DataObjectFactory<RelativeRotationBiEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("actor_rotation", SerializableDataType.enumValue(RotationType.class), RotationType.HEAD)
            .add("target_rotation", SerializableDataType.enumValue(RotationType.class), RotationType.BODY)
            .add("axes", SerializableDataTypes.AXIS_SET, EnumSet.allOf(Direction.Axis.class))
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.DOUBLE),
        data -> new RelativeRotationBiEntityConditionType(
            data.get("actor_rotation"),
            data.get("target_rotation"),
            data.get("axes"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("actor_rotation", conditionType.actorRotation)
            .set("target_rotation", conditionType.targetRotation)
            .set("axes", conditionType.axes)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final RotationType actorRotation;
    private final RotationType targetRotation;

    private final EnumSet<Direction.Axis> axes;

    private final Comparison comparison;
    private final double compareTo;

    public RelativeRotationBiEntityConditionType(RotationType actorRotation, RotationType targetRotation, EnumSet<Direction.Axis> axes, Comparison comparison, double compareTo) {
        this.actorRotation = actorRotation;
        this.targetRotation = targetRotation;
        this.axes = axes;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BiEntityConditionTypes.RELATIVE_ROTATION;
    }

    @Override
    public boolean test(Entity actor, Entity target) {
        return condition(actor, target, actorRotation, targetRotation, axes, comparison, compareTo);
    }

    public static boolean condition(Entity actor, Entity target, RotationType actorRotationType, RotationType targetRotationType, EnumSet<Direction.Axis> axes, Comparison comparison, double compareTo) {

        if (actor == null || target == null) {
            return false;
        }

        Vec3d actorRotation = actorRotationType.getRotation(actor);
        Vec3d targetRotation = targetRotationType.getRotation(target);

        actorRotation = reduceAxes(actorRotation, axes);
        targetRotation = reduceAxes(targetRotation, axes);

        return comparison.compare(getAngleBetween(actorRotation, targetRotation), compareTo);

    }

    private static double getAngleBetween(Vec3d a, Vec3d b) {
        double dot = a.dotProduct(b);
        return dot / (a.length() * b.length());
    }

    private static Vec3d reduceAxes(Vec3d vector, EnumSet<Direction.Axis> axesToKeep) {
        return new Vec3d(
            axesToKeep.contains(Direction.Axis.X) ? vector.x : 0,
            axesToKeep.contains(Direction.Axis.Y) ? vector.y : 0,
            axesToKeep.contains(Direction.Axis.Z) ? vector.z : 0
        );
    }

    private static Vec3d getBodyRotationVector(Entity entity) {

        if (!(entity instanceof LivingEntity livingEntity)) {
            return entity.getRotationVec(1.0f);
        }

        float f = livingEntity.getPitch() * ((float) Math.PI / 180);
        float g = -livingEntity.getYaw() * ((float) Math.PI / 180);

        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);

        return new Vec3d(i * j, -k, h * j);

    }

    public static ConditionTypeFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("relative_rotation"),
            new SerializableData()
                .add("actor_rotation", SerializableDataType.enumValue(RotationType.class), RotationType.HEAD)
                .add("target_rotation", SerializableDataType.enumValue(RotationType.class), RotationType.BODY)
                .add("axes", SerializableDataTypes.AXIS_SET, EnumSet.allOf(Direction.Axis.class))
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.DOUBLE),
            (data, actorAndTarget) -> condition(actorAndTarget.getLeft(), actorAndTarget.getRight(),
                data.get("actor_rotation"),
                data.get("target_rotation"),
                data.get("axes"),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

    public enum RotationType {

        HEAD(e -> e.getRotationVec(1.0F)),
        BODY(RelativeRotationBiEntityConditionType::getBodyRotationVector);

        private final Function<Entity, Vec3d> function;
        RotationType(Function<Entity, Vec3d> function) {
            this.function = function;
        }

        public Vec3d getRotation(Entity entity) {
            return function.apply(entity);
        }

    }

}
