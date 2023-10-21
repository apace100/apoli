package io.github.apace100.apoli.power.factory.condition.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class CanSeeCondition {

    public static boolean condition(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {

        Entity actor = actorAndTarget.getLeft();
        Entity target = actorAndTarget.getRight();

        if ((actor == null || target == null) || actor.getWorld() != target.getWorld()) {
            return false;
        }

        RaycastContext.ShapeType shapeType = data.get("shape_type");
        RaycastContext.FluidHandling fluidHandling = data.get("fluid_handling");

        Vec3d actorEyePos = actor.getEyePos();
        Vec3d targetEyePos = target.getEyePos();

        if (actorEyePos.distanceTo(targetEyePos) > 128.0d) {
            return false;
        }

        RaycastContext context = new RaycastContext(actorEyePos, targetEyePos, shapeType, fluidHandling, actor);
        return actor.getWorld().raycast(context).getType() == HitResult.Type.MISS;

    }

    public static ConditionFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("can_see"),
            new SerializableData()
                .add("shape_type", SerializableDataTypes.SHAPE_TYPE, RaycastContext.ShapeType.VISUAL)
                .add("fluid_handling", SerializableDataTypes.FLUID_HANDLING, RaycastContext.FluidHandling.NONE),
            CanSeeCondition::condition
        );
    }

}
