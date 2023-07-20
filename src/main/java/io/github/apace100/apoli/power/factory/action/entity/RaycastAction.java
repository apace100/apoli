package io.github.apace100.apoli.power.factory.action.entity;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class RaycastAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        Vec3d origin = new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
        Vec3d direction = entity.getRotationVec(1);
        if (data.isPresent("direction")) {
            direction = data.get("direction");
            Space space = data.get("space");
            Vector3f vector3f = new Vector3f((float)direction.getX(), (float) direction.getY(), (float) direction.getZ()).normalize();
            space.toGlobal(vector3f, entity);
            direction = new Vec3d(vector3f);
        }
        Vec3d target = origin.add(direction.multiply(getBlockReach(data, entity)));

        data.<Consumer<Entity>>ifPresent("before_action", action -> action.accept(entity));

        HitResult hitResult = null;
        if(data.getBoolean("entity")) {
            double distance = getEntityReach(data, entity);
            target = origin.add(direction.multiply(distance));
            hitResult = performEntityRaycast(entity, origin, target, data.get("bientity_condition"));
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
            if(data.isPresent("command_at_hit")) {
                Vec3d offsetDirection = direction;
                double offset = 0;
                Vec3d hitPos = hitResult.getPos();
                if(data.isPresent("command_hit_offset")) {
                    offset = data.getDouble("command_hit_offset");
                } else {
                    if(hitResult instanceof BlockHitResult bhr) {
                        if(bhr.getSide() == Direction.DOWN) {
                            offset = entity.getHeight();
                        } else if(bhr.getSide() == Direction.UP) {
                            offset = 0;
                        } else {
                            offset = entity.getWidth() / 2;
                            offsetDirection = new Vec3d(
                                bhr.getSide().getOffsetX(),
                                bhr.getSide().getOffsetY(),
                                bhr.getSide().getOffsetZ()
                            ).multiply(-1);
                        }
                    }
                    offset += 0.05;
                }
                Vec3d at = hitPos.subtract(offsetDirection.multiply(offset));
                executeCommandAtHit(entity, at, data.getString("command_at_hit"));
            }
            if(data.isPresent("command_along_ray")) {
                executeStepCommands(entity, origin, hitResult.getPos(), data.getString("command_along_ray"), data.getDouble("command_step"));
            }
            if(data.isPresent("block_action") && hitResult instanceof BlockHitResult bhr) {
                ActionFactory<Triple<World, BlockPos, Direction>>.Instance blockAction = data.get("block_action");
                Triple<World, BlockPos, Direction> blockActionContext = Triple.of(entity.getWorld(), bhr.getBlockPos(), bhr.getSide());
                blockAction.accept(blockActionContext);
            }
            if(data.isPresent("bientity_action") && hitResult instanceof EntityHitResult ehr) {
                ActionFactory<Pair<Entity, Entity>>.Instance bientityAction = data.get("bientity_action");
                Pair<Entity, Entity> bientityActionContext = new Pair<>(entity, ehr.getEntity());
                bientityAction.accept(bientityActionContext);
            }
            data.<Consumer<Entity>>ifPresent("hit_action", action -> action.accept(entity));
        } else {
            if(data.isPresent("command_along_ray") && !data.getBoolean("command_along_ray_only_on_hit")) {
                executeStepCommands(entity, origin, target, data.getString("command_along_ray"), data.getDouble("command_step"));
            }
            data.<Consumer<Entity>>ifPresent("miss_action", action -> action.accept(entity));
        }
    }

    private static double getEntityReach(SerializableData.Instance data, Entity entity) {
        if (!data.isPresent("entity_distance") && !data.isPresent("distance")) {
            double base = (entity instanceof PlayerEntity player && player.getAbilities().creativeMode) ? 6 : 3;
            return (entity instanceof LivingEntity living && FabricLoader.getInstance().isModLoaded("reach-entity-attributes")) ?
                    ReachEntityAttributes.getAttackRange(living, base) : base;
        }
        return data.isPresent("entity_distance") ? data.getDouble("entity_distance") : data.getDouble("distance");
    }


    private static double getBlockReach(SerializableData.Instance data, Entity entity) {
        if (!data.isPresent("block_distance") && !data.isPresent("distance")) {
            double base = (entity instanceof PlayerEntity player && player.getAbilities().creativeMode) ? 5.0 : 4.5;
            return (entity instanceof LivingEntity living && FabricLoader.getInstance().isModLoaded("reach-entity-attributes")) ?
                    ReachEntityAttributes.getReachDistance(living, base) : base;
        }
         return data.isPresent("block_distance") ? data.getDouble("block_distance") : data.getDouble("distance");
    }

    private static void executeStepCommands(Entity entity, Vec3d origin, Vec3d target, String command, double step) {
        MinecraftServer server = entity.getWorld().getServer();
        if(server != null) {
            Vec3d direction = target.subtract(origin).normalize();
            double length = origin.distanceTo(target);
            for(double current = 0; current < length; current += step) {
                boolean validOutput = !(entity instanceof ServerPlayerEntity) || ((ServerPlayerEntity)entity).networkHandler != null;
                ServerCommandSource source = new ServerCommandSource(
                    Apoli.config.executeCommand.showOutput && validOutput ? entity : CommandOutput.DUMMY,
                    origin.add(direction.multiply(current)),
                    entity.getRotationClient(),
                    entity.getWorld() instanceof ServerWorld ? (ServerWorld)entity.getWorld() : null,
                    Apoli.config.executeCommand.permissionLevel,
                    entity.getName().getString(),
                    entity.getDisplayName(),
                    entity.getWorld().getServer(),
                    entity);
                server.getCommandManager().executeWithPrefix(source, command);
            }
        }
    }

    private static void executeCommandAtHit(Entity entity, Vec3d hitPosition, String command) {
        MinecraftServer server = entity.getWorld().getServer();
        if(server != null) {
            boolean validOutput = !(entity instanceof ServerPlayerEntity) || ((ServerPlayerEntity)entity).networkHandler != null;
            ServerCommandSource source = new ServerCommandSource(
                Apoli.config.executeCommand.showOutput && validOutput ? entity : CommandOutput.DUMMY,
                hitPosition,
                entity.getRotationClient(),
                entity.getWorld() instanceof ServerWorld ? (ServerWorld)entity.getWorld() : null,
                Apoli.config.executeCommand.permissionLevel,
                entity.getName().getString(),
                entity.getDisplayName(),
                entity.getWorld().getServer(),
                entity);
            server.getCommandManager().executeWithPrefix(source, command);
        }
    }

    private static BlockHitResult performBlockRaycast(Entity source, Vec3d origin, Vec3d target, RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling) {
        RaycastContext context = new RaycastContext(origin, target, shapeType, fluidHandling, source);
        return source.getWorld().raycast(context);
    }

    private static EntityHitResult performEntityRaycast(Entity source, Vec3d origin, Vec3d target, ConditionFactory<Pair<Entity, Entity>>.Instance biEntityCondition) {
        Vec3d ray = target.subtract(origin);
        Box box = source.getBoundingBox().stretch(ray).expand(1.0D, 1.0D, 1.0D);
        EntityHitResult entityHitResult = ProjectileUtil.raycast(source, origin, target, box, (entityx) -> {
            return !entityx.isSpectator() && (biEntityCondition == null || biEntityCondition.test(new Pair<>(source, entityx)));
        }, ray.lengthSquared());
        return entityHitResult;
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("raycast"),
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
                .add("block_action", ApoliDataTypes.BLOCK_ACTION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("command_at_hit", SerializableDataTypes.STRING, null)
                .add("command_hit_offset", SerializableDataTypes.DOUBLE, null)
                .add("command_along_ray", SerializableDataTypes.STRING, null)
                .add("command_step", SerializableDataTypes.DOUBLE, 1.0)
                .add("command_along_ray_only_on_hit", SerializableDataTypes.BOOLEAN, false)
                .add("before_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("hit_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("miss_action", ApoliDataTypes.ENTITY_ACTION, null),
            RaycastAction::action
        );
    }
}
