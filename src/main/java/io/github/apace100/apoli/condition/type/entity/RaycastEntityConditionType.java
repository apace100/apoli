package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.function.Predicate;

public class RaycastEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<RaycastEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("match_bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("hit_bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("shape_type", SerializableDataTypes.SHAPE_TYPE, RaycastContext.ShapeType.OUTLINE)
            .add("fluid_handling", SerializableDataTypes.FLUID_HANDLING, RaycastContext.FluidHandling.ANY)
            .add("direction", SerializableDataTypes.VECTOR.optional(), Optional.empty())
            .add("space", ApoliDataTypes.SPACE, Space.WORLD)
            .add("entity_distance", SerializableDataTypes.POSITIVE_DOUBLE.optional(), Optional.empty())
            .add("block_distance", SerializableDataTypes.POSITIVE_DOUBLE.optional(), Optional.empty())
            .add("distance", SerializableDataTypes.POSITIVE_DOUBLE.optional(), Optional.empty())
            .add("entity", SerializableDataTypes.BOOLEAN, true)
            .add("block", SerializableDataTypes.BOOLEAN, true),
        data -> new RaycastEntityConditionType(
            data.get("match_bientity_condition"),
            data.get("hit_bientity_condition"),
            data.get("block_condition"),
            data.get("shape_type"),
            data.get("fluid_handling"),
            data.get("direction"),
            data.get("space"),
            data.get("entity_distance"),
            data.get("block_distance"),
            data.get("distance"),
            data.get("entity"),
            data.get("block")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("match_bientity_condition", conditionType.matchBiEntityCondition)
            .set("hit_bientity_condition", conditionType.hitBiEntityCondition)
            .set("block_condition", conditionType.blockCondition)
            .set("shape_type", conditionType.shapeType)
            .set("fluid_handling", conditionType.fluidHandling)
            .set("direction", conditionType.direction)
            .set("space", conditionType.space)
            .set("entity_distance", conditionType.entityDistance)
            .set("block_distance", conditionType.blockDistance)
            .set("distance", conditionType.distance)
            .set("entity", conditionType.entity)
            .set("block", conditionType.block)
    );

    private final Optional<BiEntityCondition> matchBiEntityCondition;
    private final Optional<BiEntityCondition> hitBiEntityCondition;

    private final Optional<BlockCondition> blockCondition;

    private final RaycastContext.ShapeType shapeType;
    private final RaycastContext.FluidHandling fluidHandling;

    private final Optional<Vec3d> direction;
    private final Space space;

    private final Optional<Double> entityDistance;
    private final Optional<Double> blockDistance;
    private final Optional<Double> distance;

    private final boolean entity;
    private final boolean block;

    public RaycastEntityConditionType(Optional<BiEntityCondition> matchBiEntityCondition, Optional<BiEntityCondition> hitBiEntityCondition, Optional<BlockCondition> blockCondition, RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling, Optional<Vec3d> direction, Space space, Optional<Double> entityDistance, Optional<Double> blockDistance, Optional<Double> distance, boolean entity, boolean block) {
        this.matchBiEntityCondition = matchBiEntityCondition;
        this.hitBiEntityCondition = hitBiEntityCondition;
        this.blockCondition = blockCondition;
        this.shapeType = shapeType;
        this.fluidHandling = fluidHandling;
        this.direction = direction;
        this.space = space;
        this.entityDistance = entityDistance;
        this.blockDistance = blockDistance;
        this.distance = distance;
        this.entity = entity;
        this.block = block;
    }

    @Override
    public boolean test(Entity entity) {

        Vec3d origin = entity.getEyePos();
        Vec3d direction = this.direction
            .map(dir -> transformDirection(entity, dir))
            .orElseGet(() -> entity.getRotationVec(1.0F));

        Vec3d destination;
        HitResult hitResult = null;

        if (this.entity) {

            double distance = getEntityReach(entity);
            destination = origin.add(direction.multiply(distance));

            hitResult = entityRaycast(entity, origin, destination);

        }

        if (this.block) {

            double distance = getBlockReach(entity);
            destination = origin.add(direction.multiply(distance));

            BlockHitResult blockResult = blockRaycast(entity, origin, destination);
            if (blockResult.getType() != HitResult.Type.MISS && overrideHitResult(entity, hitResult, blockResult)) {
                hitResult = blockResult;
            }

        }

        return switch (hitResult) {
            case BlockHitResult blockResult when blockCondition.isPresent() ->
                blockResult.getType() != HitResult.Type.MISS
                    && blockCondition.get().test(entity.getWorld(), blockResult.getBlockPos());
            case EntityHitResult entityResult when hitBiEntityCondition.isPresent() ->
                entityResult.getType() != HitResult.Type.MISS
                    && hitBiEntityCondition.get().test(entity, entityResult.getEntity());
            case null, default ->
                hitResult != null
                    && hitResult.getType() != HitResult.Type.MISS;
        };

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.RAYCAST;
    }

    private EntityHitResult entityRaycast(Entity caster, Vec3d origin, Vec3d destination) {

        Vec3d ray = destination.subtract(origin);
        Box box = caster.getBoundingBox().stretch(ray).expand(1.0D);

        Predicate<Entity> intersectPredicate = EntityPredicates.EXCEPT_SPECTATOR
            .and(intersected -> matchBiEntityCondition
                .map(condition -> condition.test(caster, intersected))
                .orElse(true));

        return ProjectileUtil.raycast(
            caster,
            origin,
            destination,
            box,
            intersectPredicate,
            ray.lengthSquared()
        );

    }

    private BlockHitResult blockRaycast(Entity caster, Vec3d origin, Vec3d destination) {
        RaycastContext context = new RaycastContext(origin, destination, shapeType, fluidHandling, caster);
        return caster.getWorld().raycast(context);
    }

    private Vec3d transformDirection(Entity entity, Vec3d direction) {

        Vector3f normalizedDirection = new Vector3f((float) direction.getX(), (float) direction.getY(), (float) direction.getZ()).normalize();
        space.toGlobal(normalizedDirection, entity);

        return new Vec3d(normalizedDirection);

    }

    private static boolean overrideHitResult(Entity caster, @Nullable HitResult prev, HitResult next) {
        return prev == null
            || prev.getType() == HitResult.Type.MISS
            || prev.squaredDistanceTo(caster) > next.squaredDistanceTo(caster);
    }

    private double getEntityReach(Entity entity) {
        return entityDistance
            .or(() -> distance)
            .orElseGet(() -> entity instanceof LivingEntity livingEntity
                ? livingEntity.getAttributeValue(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE)
                : 0.0);
    }

    private double getBlockReach(Entity entity) {
        return blockDistance
            .or(() -> distance)
            .orElseGet(() -> entity instanceof LivingEntity livingEntity
                ? livingEntity.getAttributeValue(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE)
                : 0.0);
    }

}
