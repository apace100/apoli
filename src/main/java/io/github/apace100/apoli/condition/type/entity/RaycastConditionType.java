package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3f;

import java.util.function.Predicate;

//  TODO: Refactor this to follow the format of other condition types -eggohito
public class RaycastConditionType {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        Vec3d origin = new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
        Vec3d direction = entity.getRotationVec(1);
        if (data.isPresent("direction")) {
            direction = data.get("direction");
            Space space = data.get("space");
            Vector3f vector3f = new Vector3f((float)direction.getX(), (float) direction.getY(), (float) direction.getZ()).normalize();
            space.toGlobal(vector3f, entity);
            direction = new Vec3d(vector3f);
        }
        Vec3d target;

        HitResult hitResult = null;
        if(data.getBoolean("entity")) {
            double distance = getEntityReach(data, entity);
            target = origin.add(direction.multiply(distance));
            hitResult = performEntityRaycast(entity, origin, target, data.get("match_bientity_condition"));
        }
        if(data.getBoolean("block")) {
            double distance = getBlockReach(data, entity);
            target = origin.add(direction.multiply(distance));
            BlockHitResult blockHit = performBlockRaycast(entity, origin, target, data.get("shape_type"), data.get("fluid_handling"));
            if(blockHit.getType() != HitResult.Type.MISS) {
                if(hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
                    hitResult = blockHit;
                } else {
                    if(hitResult.squaredDistanceTo(entity) > blockHit.squaredDistanceTo(entity)) {
                        hitResult = blockHit;
                    }
                }
            }
        }
        if(hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
            if(hitResult instanceof BlockHitResult bhr && data.isPresent("block_condition")) {
                CachedBlockPosition cbp = new CachedBlockPosition(entity.getWorld(), bhr.getBlockPos(), true);
                return data.<Predicate<CachedBlockPosition>>get("block_condition").test(cbp);
            }
            if(hitResult instanceof EntityHitResult ehr && data.isPresent("hit_bientity_condition")) {
                return data.<Predicate<Pair<Entity, Entity>>>get("hit_bientity_condition")
                    .test(new Pair<>(entity, ehr.getEntity()));
            }
            return true;
        }
        return false;
    }

    private static double getEntityReach(SerializableData.Instance data, Entity entity) {

        if (!data.isPresent("entity_distance") && !data.isPresent("distance")) {
            return entity instanceof LivingEntity livingEntity && livingEntity.getAttributes().hasAttribute(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE)
                ? livingEntity.getAttributeValue(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE)
                : 3;
        }

        else {
            return data.isPresent("entity_distance")
                ? data.getDouble("entity_distance")
                : data.getDouble("distance");
        }

    }


    private static double getBlockReach(SerializableData.Instance data, Entity entity) {

        if (!data.isPresent("block_distance") && !data.isPresent("distance")) {
            return entity instanceof LivingEntity livingEntity && livingEntity.getAttributes().hasAttribute(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE)
                ? livingEntity.getAttributeValue(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE)
                : 4.5;
        }

        else {
            return data.isPresent("block_distance")
                ? data.getDouble("block_distance")
                : data.getDouble("distance");
        }

    }

    private static BlockHitResult performBlockRaycast(Entity source, Vec3d origin, Vec3d target, RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling) {
        RaycastContext context = new RaycastContext(origin, target, shapeType, fluidHandling, source);
        return source.getWorld().raycast(context);
    }

    private static EntityHitResult performEntityRaycast(Entity source, Vec3d origin, Vec3d target, ConditionTypeFactory<Pair<Entity, Entity>>.Instance biEntityCondition) {
        Vec3d ray = target.subtract(origin);
        Box box = source.getBoundingBox().stretch(ray).expand(1.0D, 1.0D, 1.0D);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(source, origin, target, box, (entityx) -> {
            return !entityx.isSpectator() && (biEntityCondition == null || biEntityCondition.test(new Pair<>(source, entityx)));
        }, ray.lengthSquared());
        return entityHitResult;
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(Apoli.identifier("raycast"),
            new SerializableData()
                .add("distance", SerializableDataTypes.DOUBLE, null)
                .add("block_distance", SerializableDataTypes.DOUBLE, null)
                .add("entity_distance", SerializableDataTypes.DOUBLE, null)
                .add("direction", SerializableDataTypes.VECTOR, null)
                .add("space", ApoliDataTypes.SPACE, Space.WORLD)
                .add("block", SerializableDataTypes.BOOLEAN, true)
                .add("entity", SerializableDataTypes.BOOLEAN, true)
                .add("shape_type", SerializableDataType.enumValue(RaycastContext.ShapeType.class), RaycastContext.ShapeType.OUTLINE)
                .add("fluid_handling", SerializableDataType.enumValue(RaycastContext.FluidHandling.class), RaycastContext.FluidHandling.ANY)
                .add("match_bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("hit_bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null),
            RaycastConditionType::condition
        );
    }
}
