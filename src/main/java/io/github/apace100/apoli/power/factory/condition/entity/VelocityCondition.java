package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.function.Predicate;

public class VelocityCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        Comparison comparison = data.get("comparison");
        EnumSet<Direction.Axis> axes = data.get("axes");
        Vec3d velocity = entity.getVelocity();
        double compareTo = data.getDouble("compare_to");
        for(Direction.Axis axis : axes) {
            if(!switch(axis) {
                case X -> comparison.compare(velocity.x, compareTo);
                case Y -> comparison.compare(velocity.y, compareTo);
                case Z -> comparison.compare(velocity.z, compareTo);
            }) {
                return false;
            }
        }

        return true;
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("velocity"),
            new SerializableData()
                .add("axes", SerializableDataTypes.AXIS_SET)
                .add("compare_to", SerializableDataTypes.DOUBLE)
                .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL),
            VelocityCondition::condition
        );
    }

}
