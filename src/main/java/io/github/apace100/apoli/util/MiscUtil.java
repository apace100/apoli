package io.github.apace100.apoli.util;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

public final class MiscUtil {

    public static Optional<Entity> getEntityWithPassengers(World world, EntityType<?> entityType, @Nullable NbtCompound entityNbt, Vec3d pos, float yaw, float pitch) {

        if (world.isClient) return Optional.empty();
        ServerWorld serverWorld = (ServerWorld) world;

        NbtCompound entityToSpawnNbt = new NbtCompound();
        if (entityNbt != null) entityToSpawnNbt.copyFrom(entityNbt);
        entityToSpawnNbt.putString("id", Registries.ENTITY_TYPE.getId(entityType).toString());

        Entity entityToSpawn = EntityType.loadEntityWithPassengers(
            entityToSpawnNbt,
            serverWorld,
            entity -> {
                entity.refreshPositionAndAngles(pos.x, pos.y, pos.z, yaw, pitch);
                return entity;
            }
        );
        if (entityToSpawn == null) return Optional.empty();

        if (entityNbt == null && entityToSpawn instanceof MobEntity mobToSpawn) mobToSpawn.initialize(
            serverWorld,
            serverWorld.getLocalDifficulty(new BlockPos(pos)),
            SpawnReason.COMMAND,
            null,
            null
        );
        return Optional.of(entityToSpawn);

    }

    public static BlockState getInWallBlockState(LivingEntity playerEntity) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(int i = 0; i < 8; ++i) {
            double d = playerEntity.getX() + (double)(((float)((i >> 0) % 2) - 0.5F) * playerEntity.getWidth() * 0.8F);
            double e = playerEntity.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
            double f = playerEntity.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * playerEntity.getWidth() * 0.8F);
            mutable.set(d, e, f);
            BlockState blockState = playerEntity.world.getBlockState(mutable);
            if (blockState.getRenderType() != BlockRenderType.INVISIBLE && blockState.shouldBlockVision(playerEntity.world, mutable)) {
                return blockState;
            }
        }

        return null;
    }

    public static <T> Predicate<T> combineOr(Predicate<T> a, Predicate<T> b) {
        if(a == null) {
            return b;
        }
        if(b == null) {
            return a;
        }
        return a.or(b);
    }

    public static <T> Predicate<T> combineAnd(Predicate<T> a, Predicate<T> b) {
        if(a == null) {
            return b;
        }
        if(b == null) {
            return a;
        }
        return a.and(b);
    }
}
