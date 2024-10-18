package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
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
import net.minecraft.world.World;

import java.util.Optional;

public class RandomTeleportEntityActionType extends EntityActionType {

    public static final DataObjectFactory<RandomTeleportEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("success_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("fail_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("landing_condition", EntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("landing_block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("heightmap", SerializableDataType.enumValue(Heightmap.Type.class).optional(), Optional.empty())
            .add("landing_offset", SerializableDataTypes.VECTOR, Vec3d.ZERO)
            .add("area_width", SerializableDataTypes.POSITIVE_DOUBLE, 8.0D)
            .add("area_height", SerializableDataTypes.POSITIVE_DOUBLE, 8.0D)
            .add("loaded_chunks_only", SerializableDataTypes.BOOLEAN, true)
            .addFunctionedDefault("attempts", SerializableDataTypes.POSITIVE_INT, data -> (int) ((data.getDouble("area_width") * 2) + (data.getDouble("area_height") * 2))),
        data -> new RandomTeleportEntityActionType(
            data.get("success_action"),
            data.get("fail_action"),
            data.get("landing_condition"),
            data.get("landing_block_condition"),
            data.get("heightmap"),
            data.get("landing_offset"),
            data.getDouble("area_width") * 2,
            data.getDouble("area_height") * 2,
            data.get("loaded_chunks_only"),
            data.get("attempts")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("success_action", actionType.successAction)
            .set("fail_action", actionType.failAction)
            .set("landing_condition", actionType.landingCondition)
            .set("landing_block_condition", actionType.landingBlockCondition)
            .set("heightmap", actionType.heightmapType)
            .set("landing_offset", actionType.landingOffset)
            .set("area_width", actionType.areaWidth)
            .set("area_height", actionType.areaHeight)
            .set("loaded_chunks_only", actionType.loadedChunksOnly)
            .set("attempts", actionType.attempts)
    );

    private final Optional<EntityAction> successAction;
    private final Optional<EntityAction> failAction;

    private final Optional<EntityCondition> landingCondition;
    private final Optional<BlockCondition> landingBlockCondition;

    private final Optional<Heightmap.Type> heightmapType;
    private final Vec3d landingOffset;

    private final double areaWidth;
    private final double areaHeight;

    private final boolean loadedChunksOnly;
    private final int attempts;

    public RandomTeleportEntityActionType(Optional<EntityAction> successAction, Optional<EntityAction> failAction, Optional<EntityCondition> landingCondition, Optional<BlockCondition> landingBlockCondition, Optional<Heightmap.Type> heightmapType, Vec3d landingOffset, double areaWidth, double areaHeight, boolean loadedChunksOnly, int attempts) {
        this.successAction = successAction;
        this.failAction = failAction;
        this.landingCondition = landingCondition;
        this.landingBlockCondition = landingBlockCondition;
        this.heightmapType = heightmapType;
        this.landingOffset = landingOffset;
        this.loadedChunksOnly = loadedChunksOnly;
        this.attempts = attempts;
        this.areaWidth = areaWidth;
        this.areaHeight = areaHeight;
    }

    @Override
    protected void execute(Entity entity) {

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        Random random = Random.create();
        boolean succeeded = false;

        double x, y, z;

        for (int i = 0; i < attempts; i++) {

            x = entity.getX() + (random.nextDouble() - 0.5) * areaWidth;
            y = MathHelper.clamp(entity.getY() + (random.nextInt(Math.max((int) areaHeight, 1)) - (areaHeight / 2)), serverWorld.getBottomY(), serverWorld.getBottomY() + (serverWorld.getLogicalHeight() - 1));
            z = entity.getZ() + (random.nextDouble() - 0.5) * areaWidth;

            if (attemptToTeleport(entity, serverWorld, x, y, z)) {

                successAction.ifPresent(action -> action.execute(entity));
                entity.onLanding();

                succeeded = true;
                break;

            }

        }

        if (!succeeded) {
            failAction.ifPresent(action -> action.execute(entity));
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.RANDOM_TELEPORT;
    }

    private boolean attemptToTeleport(Entity entity, ServerWorld serverWorld, double destX, double destY, double destZ) {

        BlockPos.Mutable blockPos = BlockPos.ofFloored(destX, destY, destZ).mutableCopy();
        boolean foundSurface = false;

        if (heightmapType.isPresent()) {

            blockPos.set(serverWorld.getTopPosition(heightmapType.get(), blockPos).down());
            foundSurface |= shouldLandOnBlock(serverWorld, blockPos);

            if (foundSurface) {
                blockPos.set(blockPos.up());
            }

        }

        else {

            for (double decrements = 0; decrements < areaHeight / 2 && !foundSurface; ++decrements) {

                blockPos.set(blockPos.down());
                foundSurface = shouldLandOnBlock(serverWorld, blockPos);

                if (foundSurface) {
                    blockPos.set(blockPos.up());
                }

            }

        }

        destX = landingOffset.getX() == 0 ? destX : MathHelper.floor(destX) + landingOffset.getX();
        destY = blockPos.getY() + landingOffset.getY();
        destZ = landingOffset.getZ() == 0 ? destZ : MathHelper.floor(destZ) + landingOffset.getZ();

        blockPos.set(destX, destY, destZ);

        if (!foundSurface) {
            return false;
        }

        double prevX = entity.getX();
        double prevY = entity.getY();
        double prevZ = entity.getZ();

        ChunkPos chunkPos = new ChunkPos(blockPos);
        if (!loadedChunksOnly && !serverWorld.isChunkLoaded(chunkPos.x, chunkPos.z)) {
            serverWorld.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 0, entity.getId());
            serverWorld.getChunk(chunkPos.x, chunkPos.z);
        }

        entity.requestTeleport(destX, destY, destZ);

        if (shouldLand(entity)) {
            entity.requestTeleport(prevX, prevY, prevZ);
            return false;
        }

        if (entity instanceof PathAwareEntity pathAwareEntity) {
            pathAwareEntity.getNavigation().stop();
        }

        return true;

    }

    private boolean shouldLandOnBlock(World world, BlockPos pos) {
        return landingBlockCondition
            .map(condition -> condition.test(world, pos))
            .orElseGet(() -> world.getBlockState(pos).blocksMovement());
    }

    private boolean shouldLand(Entity entity) {
        return landingCondition
            .map(condition -> condition.test(entity))
            .orElseGet(() -> entity.getWorld().isSpaceEmpty(entity) && !entity.getWorld().containsFluid(entity.getBoundingBox()));
    }

}
