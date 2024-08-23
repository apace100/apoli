package io.github.apace100.apoli.condition.type.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class CanSeeConditionType {

    public static boolean condition(Entity actor, Entity target, RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling) {

        if ((actor == null || target == null) || actor.getWorld() != target.getWorld()) {
            return false;
        }

        Vec3d actorEyePos = actor.getEyePos();
        Vec3d targetEyePos = target.getEyePos();

        if (actorEyePos.distanceTo(targetEyePos) > 128.0d) {
            return false;
        }

        RaycastContext context = new RaycastContext(actorEyePos, targetEyePos, shapeType, fluidHandling, actor);
        return actor.getWorld().raycast(context).getType() == HitResult.Type.MISS;

    }

    public static ConditionTypeFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("can_see"),
            new SerializableData()
                .add("shape_type", SerializableDataTypes.SHAPE_TYPE, RaycastContext.ShapeType.VISUAL)
                .add("fluid_handling", SerializableDataTypes.FLUID_HANDLING, RaycastContext.FluidHandling.NONE),
            (data, actorAndTarget) -> condition(actorAndTarget.getLeft(), actorAndTarget.getRight(),
                data.get("shape_type"),
                data.get("fluid_handling")
            )
        );
    }

}
