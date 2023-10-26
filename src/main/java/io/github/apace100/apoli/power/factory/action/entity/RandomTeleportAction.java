package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class RandomTeleportAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        Predicate<CachedBlockPosition> landingBlockCondition = data.isPresent("landing_block_condition") ? data.get("landing_block_condition")
            : cachedBlockPosition -> cachedBlockPosition.getBlockState().blocksMovement();
        Predicate<CachedBlockPosition> safeBlockCondition = data.isPresent("safe_block_condition") ? data.get("safe_block_condition")
            : cachedBlockPosition -> serverWorld.isSpaceEmpty(entity) && !serverWorld.containsFluid(entity.getBoundingBox());

        Heightmap.Type heightmap = data.get("heightmap");
        Random random = Random.create();

        boolean succeeded = false;
        int attempts = data.getInt("attempts");

        double areaWidth = data.getDouble("area_width") * 2;
        double areaHeight = data.getDouble("area_height") * 2;
        double x, y, z;

        for (int i = 0; i < attempts; i++) {

            x = entity.getX() + (random.nextDouble() - 0.5) * areaWidth;
            y = MathHelper.clamp(entity.getY() + (random.nextInt(Math.max((int) areaHeight, 1)) - (areaHeight / 2)), serverWorld.getBottomY(), serverWorld.getBottomY() + (serverWorld.getLogicalHeight() - 1));
            z = entity.getZ() + (random.nextDouble() - 0.5) * areaWidth;

            if (attemptToTeleport(entity, serverWorld, x, y, z, areaHeight, heightmap, landingBlockCondition, safeBlockCondition)) {

                data.<Consumer<Entity>>ifPresent("success_action", successAction -> successAction.accept(entity));
                entity.onLanding();

                succeeded = true;
                break;

            }

        }

        if (!succeeded) {
            data.<Consumer<Entity>>ifPresent("fail_action", failAction -> failAction.accept(entity));
        }

    }

    private static boolean attemptToTeleport(Entity entity, ServerWorld serverWorld, double destX, double destY, double destZ, double areaHeight, Heightmap.Type heightmap, Predicate<CachedBlockPosition> landingBlockCondition, Predicate<CachedBlockPosition> safeBlockCondition) {

        BlockPos.Mutable blockPos = BlockPos.ofFloored(destX, destY, destZ).mutableCopy();

        boolean safelyTeleported = false;
        boolean teleported = false;

        double prevX = entity.getX();
        double prevY = entity.getY();
        double prevZ = entity.getZ();

        if (serverWorld.isChunkLoaded(ChunkSectionPos.getSectionCoord(blockPos.getX()), ChunkSectionPos.getSectionCoord(blockPos.getZ()))) {

            boolean foundSurface = false;
            int increments = 0;

            while (heightmap == null && increments < areaHeight) {

                blockPos.set(blockPos.down());

                if (landingBlockCondition.test(new CachedBlockPosition(serverWorld, blockPos, true))) {

                    blockPos.set(blockPos.up());
                    foundSurface = true;

                    break;

                }

                ++increments;

            }

            if (heightmap != null) {

                blockPos.set(serverWorld.getTopPosition(heightmap, blockPos).down());

                if (landingBlockCondition.test(new CachedBlockPosition(serverWorld, blockPos, true))) {
                    blockPos.set(blockPos.up());
                    foundSurface = true;
                }

            }

            if (foundSurface) {

                entity.requestTeleport(destX, blockPos.getY(), destZ);
                teleported = true;

                if (safeBlockCondition.test(new CachedBlockPosition(serverWorld, blockPos, true))) {
                    safelyTeleported = true;
                }

            }

        }

        if (teleported) {

            if (!safelyTeleported) {
                entity.requestTeleport(prevX, prevY, prevZ);
                return false;
            }

            else if (entity instanceof PathAwareEntity pathAwareEntity) {
                pathAwareEntity.getNavigation().stop();
            }

            return true;

        }

        return false;

    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("random_teleport"),
            new SerializableData()
                .add("area_width", SerializableDataTypes.DOUBLE, 8.0)
                .add("area_height", SerializableDataTypes.DOUBLE, 8.0)
                .add("heightmap", SerializableDataType.enumValue(Heightmap.Type.class), null)
                .addFunctionedDefault("attempts", SerializableDataTypes.INT, data -> (int) (data.getDouble("area_width") + data.getDouble("area_height")))
                .add("landing_block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("safe_block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("success_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("fail_action", ApoliDataTypes.ENTITY_ACTION, null),
            RandomTeleportAction::action
        );
    }

}
