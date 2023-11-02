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
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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
        Predicate<Entity> landingCondition = data.isPresent("landing_condition") ? data.get("landing_condition")
            : _entity -> serverWorld.isSpaceEmpty(_entity) && !serverWorld.containsFluid(_entity.getBoundingBox());

        Heightmap.Type heightmap = data.get("heightmap");
        Random random = Random.create();
        Vec3d landingOffset = data.get("landing_offset");

        boolean loadedChunksOnly = data.getBoolean("loaded_chunks_only");
        boolean succeeded = false;

        int attempts = data.getInt("attempts");

        double areaWidth = data.getDouble("area_width") * 2;
        double areaHeight = data.getDouble("area_height") * 2;
        double x, y, z;

        for (int i = 0; i < attempts; i++) {

            x = entity.getX() + (random.nextDouble() - 0.5) * areaWidth;
            y = MathHelper.clamp(entity.getY() + (random.nextInt(Math.max((int) areaHeight, 1)) - (areaHeight / 2)), serverWorld.getBottomY(), serverWorld.getBottomY() + (serverWorld.getLogicalHeight() - 1));
            z = entity.getZ() + (random.nextDouble() - 0.5) * areaWidth;

            if (attemptToTeleport(entity, serverWorld, x, y, z, landingOffset.getX(), landingOffset.getY(), landingOffset.getZ(), areaHeight, loadedChunksOnly, heightmap, landingBlockCondition, landingCondition)) {

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

    private static boolean attemptToTeleport(Entity entity, ServerWorld serverWorld, double destX, double destY, double destZ, double offsetX, double offsetY, double offsetZ, double areaHeight, boolean loadedChunksOnly, Heightmap.Type heightmap, Predicate<CachedBlockPosition> landingBlockCondition, Predicate<Entity> landingCondition) {

        BlockPos.Mutable blockPos = BlockPos.ofFloored(destX, destY, destZ).mutableCopy();
        boolean foundSurface = false;

        if (heightmap != null) {

            blockPos.set(serverWorld.getTopPosition(heightmap, blockPos).down());

            if (landingBlockCondition.test(new CachedBlockPosition(serverWorld, blockPos, true))) {
                blockPos.set(blockPos.up());
                foundSurface = true;
            }

        } else {

            for (double decrements = 0; decrements < areaHeight / 2; ++decrements) {

                blockPos.set(blockPos.down());

                if (landingBlockCondition.test(new CachedBlockPosition(serverWorld, blockPos, true))) {

                    blockPos.set(blockPos.up());
                    foundSurface = true;

                    break;

                }

            }

        }

        destX = offsetX == 0 ? destX : MathHelper.floor(destX) + offsetX;
        destY = blockPos.getY() + offsetY;
        destZ = offsetZ == 0 ? destZ : MathHelper.floor(destZ) + offsetZ;

        blockPos.set(destX, destY, destZ);

        if (!foundSurface) {
            return false;
        }

        double prevX = entity.getX();
        double prevY = entity.getY();
        double prevZ = entity.getZ();

        ChunkPos chunkPos = new ChunkPos(blockPos);
        if (!serverWorld.isChunkLoaded(chunkPos.x, chunkPos.z)) {

            if (loadedChunksOnly) {
                return false;
            }

            serverWorld.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 0, entity.getId());
            serverWorld.getChunk(chunkPos.x, chunkPos.z);

        }

        entity.requestTeleport(destX, destY, destZ);

        if (!landingCondition.test(entity)) {
            entity.requestTeleport(prevX, prevY, prevZ);
            return false;
        }

        if (entity instanceof PathAwareEntity pathAwareEntity) {
            pathAwareEntity.getNavigation().stop();
        }

        return true;

    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("random_teleport"),
            new SerializableData()
                .add("area_width", SerializableDataTypes.DOUBLE, 8.0)
                .add("area_height", SerializableDataTypes.DOUBLE, 8.0)
                .add("heightmap", SerializableDataType.enumValue(Heightmap.Type.class), null)
                .addFunctionedDefault("attempts", SerializableDataTypes.INT, data -> (int) ((data.getDouble("area_width") * 2) + (data.getDouble("area_height") * 2)))
                .add("landing_block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("landing_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                .add("landing_offset", SerializableDataTypes.VECTOR, Vec3d.ZERO)
                .add("loaded_chunks_only", SerializableDataTypes.BOOLEAN, true)
                .add("success_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("fail_action", ApoliDataTypes.ENTITY_ACTION, null),
            RandomTeleportAction::action
        );
    }

}
