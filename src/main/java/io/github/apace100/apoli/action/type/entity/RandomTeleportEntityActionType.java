package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
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
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RandomTeleportEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<RandomTeleportEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
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
            data.getDouble("area_width"),
            data.getDouble("area_height"),
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
        this.areaWidth = areaWidth * 2;
        this.areaHeight = areaHeight * 2;
    }

    @Override
    public void accept(EntityActionContext context) {

        Entity entity = context.entity();
        World world = entity.getWorld();

        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        Random random = Random.create();
        boolean succeeded = false;

        double x, y, z;

        for (int i = 0; i < attempts; i++) {

            x = entity.getX() + (random.nextDouble() - 0.5) * areaWidth;
            y = MathHelper.clamp(entity.getY() + (random.nextInt(Math.max((int) areaHeight, 1)) - (areaHeight / 2)), serverWorld.getBottomY(), serverWorld.getBottomY() + (serverWorld.getLogicalHeight() - 1));
            z = entity.getZ() + (random.nextDouble() - 0.5) * areaWidth;

            if (this.attemptToTeleport(entity, serverWorld, x, y, z)) {

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
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.RANDOM_TELEPORT;
    }

    private boolean attemptToTeleport(Entity entity, ServerWorld serverWorld, double destX, double destY, double destZ) {

        BlockPos.Mutable destBlockPos = BlockPos.ofFloored(destX, destY, destZ).mutableCopy();
        boolean foundSurface = false;

        if (heightmapType.isPresent()) {

            destBlockPos.set(serverWorld.getTopPosition(heightmapType.get(), destBlockPos).down());
            foundSurface = this.shouldLandOnBlock(serverWorld, destBlockPos);

            if (foundSurface) {
                destBlockPos.set(destBlockPos.up());
            }

        }

        for (double decrements = 0; !foundSurface && decrements < areaHeight / 2; ++decrements) {

            destBlockPos.set(destBlockPos.down());
            foundSurface = this.shouldLandOnBlock(serverWorld, destBlockPos);

            if (foundSurface) {
                destBlockPos.set(destBlockPos.up());
            }

        }

        if (!foundSurface) {
            return false;
        }

        destX = landingOffset.getX() == 0 ? destX : MathHelper.floor(destX) + landingOffset.getX();
        destY = destBlockPos.getY() + landingOffset.getY();
        destZ = landingOffset.getZ() == 0 ? destZ : MathHelper.floor(destZ) + landingOffset.getZ();

        destBlockPos.set(destX, destY, destZ);

        double prevX = entity.getX();
        double prevY = entity.getY();
        double prevZ = entity.getZ();

        ChunkPos destChunkPos = new ChunkPos(destBlockPos);
        if (!loadedChunksOnly && !serverWorld.isChunkLoaded(destChunkPos.x, destChunkPos.z)) {
            serverWorld.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, destChunkPos, 0, entity.getId());
            serverWorld.getChunk(destChunkPos.x, destChunkPos.z);
        }

        entity.requestTeleport(destX, destY, destZ);

        if (!this.shouldLand(entity)) {
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
