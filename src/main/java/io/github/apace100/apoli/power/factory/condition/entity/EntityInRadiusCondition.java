package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.Shape;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Predicate;

public class EntityInRadiusCondition {

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
                Apoli.identifier("entity_in_radius"),
                new SerializableData()
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE)
                        .add("radius", SerializableDataTypes.FLOAT)
                        .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
                        .add("compare_to", SerializableDataTypes.INT, 1),
                EntityInRadiusCondition::condition
        );
    }

    private static boolean condition(SerializableData.Instance data, Entity actor) {
        // Get parameters from data
        Predicate<Pair<Entity, Entity>> bientityCondition = data.get("bientity_condition");
        Shape shape = data.get("shape");
        float radius = data.getFloat("radius");
        Comparison comparison = data.get("comparison");
        int compareTo = data.getInt("compare_to");

        // Calculate center position at actor's feet
        Vec3d center = new Vec3d(actor.getX(), actor.getBoundingBox().minY, actor.getZ());

        // Create bounding box that covers the entire area
        double diameter = radius * 2;
        Box box = Box.of(center, diameter, diameter, diameter);

        // Get entities in the bounding box
        List<Entity> entities = actor.getWorld().getOtherEntities(actor, box);

        int count = 0;
        for (Entity target : entities) {
            // Calculate target's foot position
            Vec3d targetFeet = new Vec3d(target.getX(), target.getBoundingBox().minY, target.getZ());

            // Calculate differences
            double dx = Math.abs(center.x - targetFeet.x);
            double dy = Math.abs(center.y - targetFeet.y);
            double dz = Math.abs(center.z - targetFeet.z);

            // Calculate distance based on shape
            double distance = Shape.getDistance(shape, dx, dy, dz);

            // Check if within radius and satisfies bi-entity condition
            if (distance <= radius &&
                    (bientityCondition == null || bientityCondition.test(new Pair<>(actor, target)))) {
                count++;
            }
        }

        // Compare count using the specified comparison
        return comparison.compare(count, compareTo);
    }
}