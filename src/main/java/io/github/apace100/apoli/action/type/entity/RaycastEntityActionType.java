package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class RaycastEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<RaycastEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("before_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("hit_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("miss_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("block_action", BlockAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("shape_type", SerializableDataTypes.SHAPE_TYPE, RaycastContext.ShapeType.OUTLINE)
            .add("fluid_handling", SerializableDataTypes.FLUID_HANDLING, RaycastContext.FluidHandling.ANY)
            .add("direction", SerializableDataTypes.VECTOR.optional(), Optional.empty())
            .add("space", ApoliDataTypes.SPACE, Space.WORLD)
            .add("entity_distance", SerializableDataTypes.POSITIVE_DOUBLE.optional(), Optional.empty())
            .add("block_distance", SerializableDataTypes.POSITIVE_DOUBLE.optional(), Optional.empty())
            .add("distance", SerializableDataTypes.POSITIVE_DOUBLE.optional(), Optional.empty())
            .add("command_at_hit", SerializableDataTypes.STRING.optional(), Optional.empty())
            .add("command_along_ray", SerializableDataTypes.STRING.optional(), Optional.empty())
            .add("command_hit_offset", SerializableDataTypes.DOUBLE.optional(), Optional.empty())
            .add("command_step", SerializableDataTypes.DOUBLE, 1.0D)
            .add("command_along_ray_only_on_hit", SerializableDataTypes.BOOLEAN, false)
            .add("entity", SerializableDataTypes.BOOLEAN, true)
            .add("block", SerializableDataTypes.BOOLEAN, true),
        data -> new RaycastEntityActionType(
            data.get("before_action"),
            data.get("hit_action"),
            data.get("miss_action"),
            data.get("bientity_action"),
            data.get("block_action"),
            data.get("bientity_condition"),
            data.get("shape_type"),
            data.get("fluid_handling"),
            data.get("direction"),
            data.get("space"),
            data.get("entity_distance"),
            data.get("block_distance"),
            data.get("distance"),
            data.get("command_at_hit"),
            data.get("command_along_ray"),
            data.get("command_hit_offset"),
            data.get("command_step"),
            data.get("command_along_ray_only_on_hit"),
            data.get("entity"),
            data.get("block")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("before_action", actionType.beforeAction)
            .set("hit_action", actionType.hitAction)
            .set("miss_action", actionType.missAction)
            .set("bientity_action", actionType.biEntityAction)
            .set("block_action", actionType.blockAction)
            .set("bientity_condition", actionType.biEntityCondition)
            .set("shape_type", actionType.shapeType)
            .set("fluid_handling", actionType.fluidHandling)
            .set("direction", actionType.direction)
            .set("space", actionType.space)
            .set("entity_distance", actionType.entityDistance)
            .set("block_distance", actionType.blockDistance)
            .set("distance", actionType.distance)
            .set("command_at_hit", actionType.commandAtHit)
            .set("command_along_ray", actionType.commandAlongRay)
            .set("command_hit_offset", actionType.commandHitOffset)
            .set("command_step", actionType.commandStep)
            .set("command_along_ray_only_on_hit", actionType.commandAlongRayOnlyOnHit)
            .set("entity", actionType.entity)
            .set("block", actionType.block)
    );

    private final Optional<EntityAction> beforeAction;
    private final Optional<EntityAction> hitAction;
    private final Optional<EntityAction> missAction;

    private final Optional<BiEntityAction> biEntityAction;
    private final Optional<BlockAction> blockAction;

    private final Optional<BiEntityCondition> biEntityCondition;

    private final RaycastContext.ShapeType shapeType;
    private final RaycastContext.FluidHandling fluidHandling;

    private final Optional<Vec3d> direction;
    private final Space space;

    private final Optional<Double> entityDistance;
    private final Optional<Double> blockDistance;
    private final Optional<Double> distance;

    private final Optional<String> commandAtHit;
    private final Optional<String> commandAlongRay;

    private final Optional<Double> commandHitOffset;
    private final double commandStep;

    private final boolean commandAlongRayOnlyOnHit;

    private final boolean entity;
    private final boolean block;

    public RaycastEntityActionType(Optional<EntityAction> beforeAction, Optional<EntityAction> hitAction, Optional<EntityAction> missAction, Optional<BiEntityAction> biEntityAction, Optional<BlockAction> blockAction, Optional<BiEntityCondition> biEntityCondition, RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling, Optional<Vec3d> direction, Space space, Optional<Double> entityDistance, Optional<Double> blockDistance, Optional<Double> distance, Optional<String> commandAtHit, Optional<String> commandAlongRay, Optional<Double> commandHitOffset, double commandStep, boolean commandAlongRayOnlyOnHit, boolean entity, boolean block) {
        this.beforeAction = beforeAction;
        this.hitAction = hitAction;
        this.missAction = missAction;
        this.biEntityAction = biEntityAction;
        this.blockAction = blockAction;
        this.biEntityCondition = biEntityCondition;
        this.shapeType = shapeType;
        this.fluidHandling = fluidHandling;
        this.direction = direction;
        this.space = space;
        this.entityDistance = entityDistance;
        this.blockDistance = blockDistance;
        this.distance = distance;
        this.commandAtHit = commandAtHit;
        this.commandAlongRay = commandAlongRay;
        this.commandHitOffset = commandHitOffset;
        this.commandStep = commandStep;
        this.commandAlongRayOnlyOnHit = commandAlongRayOnlyOnHit;
        this.entity = entity;
        this.block = block;
    }

    @Override
    public void accept(EntityActionContext context) {

        beforeAction.ifPresent(self -> self.accept(context));

        Entity entity = context.entity();
        boolean success = false;

        HitResult result = null;
        Vec3d directionVec = direction
            .map(self -> this.transformDirection(entity, self))
            .orElseGet(() -> entity.getRotationVec(1.0F));

        Vec3d origin = MiscUtil.getPoseDependentEyePos(entity).add(context.offset());
        Vec3d destination = distance.map(directionVec::multiply).orElse(null);

        if (this.entity) {
            destination = origin.add(directionVec.multiply(this.getEntityReach(entity)));
            result = this.entityRaycast(entity, origin, destination);
        }

        if (this.block) {

            Vec3d blockDestination = origin.add(directionVec.multiply(this.getBlockReach(entity)));
            BlockHitResult blockResult = this.blockRaycast(entity, origin, blockDestination);

            if (blockResult.getType() != HitResult.Type.MISS && overrideHitResult(entity, result, blockResult)) {
                destination = blockDestination;
                result = blockResult;
            }

        }

        if (result != null && result.getType() != HitResult.Type.MISS) {

            success = true;

            if (commandAtHit.isPresent()) {

                Offset offset = this.getOffset(entity, result, directionVec);
                Vec3d hitPos = offset.apply(result.getPos());

                this.executeCommandAtHit(entity, hitPos);

            }

        }

        if (destination != null && commandAlongRay.isPresent() && (!commandAlongRayOnlyOnHit || success)) {
            this.executeCommandAtSteps(entity, origin, success ? result.getPos() : destination);
        }

        if (success) {

            switch (result) {
                case BlockHitResult blockResult ->
                    blockAction.ifPresent(self -> self.execute(entity.getWorld(), blockResult.getBlockPos(), Optional.of(blockResult.getSide())));
                case EntityHitResult entityResult ->
                    biEntityAction.ifPresent(self -> self.execute(entity, entityResult.getEntity()));
                default -> {
                    //  No-op; unsupported
                }
            }

            hitAction.ifPresent(self -> self.execute(entity));

        }

        else {
            missAction.ifPresent(self -> self.execute(entity));
        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.RAYCAST;
    }

    private record Offset(Vec3d direction, double amount) implements UnaryOperator<Vec3d> {

        @Override
        public Vec3d apply(Vec3d vec) {
            return vec.subtract(direction().multiply(amount()));
        }

    }

    private EntityHitResult entityRaycast(Entity caster, Vec3d origin, Vec3d destination) {

        Vec3d ray = destination.subtract(origin);
        Box box = caster.getBoundingBox().stretch(ray).expand(1.0D);

        Predicate<Entity> intersectPredicate = EntityPredicates.EXCEPT_SPECTATOR
            .and(intersected -> biEntityCondition
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

    private Offset getOffset(Entity entity, HitResult hitResult, Vec3d direction) {

        if (commandHitOffset.isPresent()) {
            return new Offset(direction, commandHitOffset.get());
        }

        else {

            Vec3d offsetDirection = direction;
            double offset = 0.0D;

            if (hitResult instanceof BlockHitResult blockResult) {

                Direction hitSide = blockResult.getSide();

                switch (hitSide) {
                    case DOWN ->
                        offset = entity.getHeight();
                    case UP ->
                        offset = 0;
                    default -> {

                        double offsetX = hitSide.getOffsetX();
                        double offsetY = hitSide.getOffsetY();
                        double offsetZ = hitSide.getOffsetZ();

                        offset = entity.getWidth() / 2;
                        offsetDirection = new Vec3d(offsetX, offsetY, offsetZ).negate();

                    }
                }

            }

            offset += 0.05;
            return new Offset(offsetDirection, offset);

        }

    }

    private static boolean overrideHitResult(Entity caster, @Nullable HitResult prev, HitResult next) {
        return prev == null
            || prev.getType() == HitResult.Type.MISS
            || prev.squaredDistanceTo(caster) > next.squaredDistanceTo(caster);
    }

    private double getEntityReach(Entity entity) {
        return entityDistance
            .or(() -> distance)
            .orElseGet(() -> MiscUtil.getAttributeValueOrElse(entity, EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, 1.0));
    }

    private double getBlockReach(Entity entity) {
        return blockDistance
            .or(() -> distance)
            .orElseGet(() -> MiscUtil.getAttributeValueOrElse(entity, EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE, 1.0));
    }

    private void executeCommandAtSteps(Entity entity, Vec3d origin, Vec3d destination) {

        MinecraftServer server = entity.getServer();
        String commandAlongRay = this.commandAlongRay.orElse("");

        if (server == null || commandAlongRay.isEmpty()) {
            return;
        }

        Vec3d direction = destination.subtract(origin).normalize();
        double distance = origin.distanceTo(destination);

        ServerCommandSource commandSource = entity.getCommandSource()
            .withOutput(CommandOutput.DUMMY)
            .withLevel(Apoli.config.executeCommand.permissionLevel);

        if (Apoli.config.executeCommand.showOutput) {
            commandSource = commandSource.withOutput(entity instanceof ServerPlayerEntity serverPlayer && serverPlayer.networkHandler != null
                ? serverPlayer
                : server);
        }

        for (double steps = 0; steps < distance; steps += commandStep) {

            Vec3d offsetPos = direction.multiply(steps);
            Vec3d newPos = origin.add(offsetPos);

            server.getCommandManager().executeWithPrefix(commandSource.withPosition(newPos), commandAlongRay);

        }

    }

    private void executeCommandAtHit(Entity entity, Vec3d hitPos) {

        MinecraftServer server = entity.getServer();
        String commandAtHit = this.commandAtHit.orElse("");

        if (server == null || commandAtHit.isEmpty()) {
            return;
        }

        ServerCommandSource commandSource = entity.getCommandSource()
            .withOutput(CommandOutput.DUMMY)
            .withPosition(hitPos)
            .withLevel(Apoli.config.executeCommand.permissionLevel);

        if (Apoli.config.executeCommand.showOutput) {
            commandSource = commandSource.withOutput(entity instanceof ServerPlayerEntity serverPlayer && serverPlayer.networkHandler != null
                ? serverPlayer
                : server);
        }

        server.getCommandManager().executeWithPrefix(commandSource, commandAtHit);

    }

}
