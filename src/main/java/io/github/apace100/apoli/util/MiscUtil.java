package io.github.apace100.apoli.util;

import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class MiscUtil {

    public static void createExplosion(World world, Vec3d pos, float power, boolean createFire, Explosion.DestructionType destructionType, ExplosionBehavior behavior) {
        createExplosion(world, null, pos, power, createFire, destructionType, behavior);
    }

    public static void createExplosion(World world, Entity entity, Vec3d pos, float power, boolean createFire, Explosion.DestructionType destructionType, ExplosionBehavior behavior) {
        createExplosion(world, entity, null, pos.getX(), pos.getY(), pos.getZ(), power, createFire, destructionType, behavior);
    }

    public static void createExplosion(World world, @Nullable Entity entity, @Nullable DamageSource damageSource, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType destructionType, ExplosionBehavior behavior) {

        Explosion explosion = new Explosion(world, entity, damageSource, behavior, x, y, z, power, createFire, destructionType, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.ENTITY_GENERIC_EXPLODE);

        explosion.collectBlocksAndDamageEntities();
        explosion.affectWorld(world.isClient);

        //  Sync the explosion effect to the client if the explosion is created on the server
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        if (!explosion.shouldDestroy()) {
            explosion.clearAffectedBlocks();
        }

        for (ServerPlayerEntity serverPlayerEntity : serverWorld.getPlayers()) {
            if (serverPlayerEntity.squaredDistanceTo(x, y, z) < 4096.0) {
                serverPlayerEntity.networkHandler.sendPacket(new ExplosionS2CPacket(x, y, z, power, explosion.getAffectedBlocks(), explosion.getAffectedPlayers().get(serverPlayerEntity), explosion.getDestructionType(), explosion.getParticle(), explosion.getEmitterParticle(), explosion.getSoundEvent()));
            }
        }

    }

    @Nullable
    public static ExplosionBehavior getExplosionBehavior(World world, float indestructibleResistance, @Nullable Predicate<CachedBlockPosition> indestructibleCondition) {
        return indestructibleCondition == null ? null : new ExplosionBehavior() {

            @Override
            public Optional<Float> getBlastResistance(Explosion explosion, BlockView blockView, BlockPos pos, BlockState blockState, FluidState fluidState) {

                CachedBlockPosition cachedBlockPosition = new CachedBlockPosition(world, pos, true);

                Optional<Float> defaultValue = super.getBlastResistance(explosion, world, pos, blockState, fluidState);
                Optional<Float> newValue = indestructibleCondition.test(cachedBlockPosition) ? Optional.of(indestructibleResistance) : Optional.empty();

                return defaultValue.isPresent() ? (newValue.isPresent() ? (defaultValue.get() > newValue.get() ? (defaultValue) : newValue) : defaultValue) : defaultValue;

            }

            @Override
            public boolean canDestroyBlock(Explosion explosion, BlockView blockView, BlockPos pos, BlockState state, float power) {
                return !indestructibleCondition.test(new CachedBlockPosition(world, pos, true));
            }

        };
    }

    @Nullable
    public static ExplosionBehavior createExplosionBehavior(@Nullable Predicate<BlockConditionContext> indestructibleCondition, float resistance) {
        return indestructibleCondition == null ? null : new ExplosionBehavior() {

            @Override
            public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {

                Optional<Float> defaultValue = super.getBlastResistance(explosion, world, pos, blockState, fluidState);
                Optional<Float> newValue = indestructibleCondition.test(new BlockConditionContext((World) world, pos))
                    ? Optional.of(resistance)
                    : Optional.empty();

                return defaultValue
                    .flatMap(defVal -> newValue
                        .map(newVal -> defVal > newVal ? defVal : newVal));

            }

            @Override
            public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
                return !indestructibleCondition.test(new BlockConditionContext((World) world, pos));
            }

        };
    }

    public static Optional<Entity> getEntityWithPassengers(World world, EntityType<?> entityType, @Nullable NbtCompound entityNbt, Vec3d pos, float yaw, float pitch) {
        return getEntityWithPassengers(world, entityType, entityNbt, pos, Optional.of(yaw), Optional.of(pitch));
    }

    public static Optional<Entity> getEntityWithPassengers(World world, EntityType<?> entityType, @Nullable NbtCompound entityNbt, Vec3d pos, Optional<Float> yaw, Optional<Float> pitch) {

        if (!(world instanceof ServerWorld serverWorld)) {
            return Optional.empty();
        }

        NbtCompound entityToSpawnNbt = new NbtCompound();
        if (entityNbt != null && !entityNbt.isEmpty()) {
            entityToSpawnNbt.copyFrom(entityNbt);
        }

        entityToSpawnNbt.putString("id", Registries.ENTITY_TYPE.getId(entityType).toString());
        Entity entityToSpawn = EntityType.loadEntityWithPassengers(
            entityToSpawnNbt,
            serverWorld,
            entity -> {
                entity.refreshPositionAndAngles(pos.x, pos.y, pos.z, yaw.orElse(entity.getYaw()), pitch.orElse(entity.getPitch()));
                return entity;
            }
        );

        if (entityToSpawn == null) {
            return Optional.empty();
        }

        if ((entityNbt == null || entityNbt.isEmpty()) && entityToSpawn instanceof MobEntity mobToSpawn) {
            mobToSpawn.initialize(serverWorld, serverWorld.getLocalDifficulty(BlockPos.ofFloored(pos)), SpawnReason.COMMAND, null);
        }

        return Optional.of(entityToSpawn);

    }

    @Nullable
    public static Entity getEntityByUuid(@Nullable UUID uuid, @Nullable MinecraftServer server) {

        if (uuid == null || server == null) {
            return null;
        }

        Entity entity;
        for (ServerWorld serverWorld : server.getWorlds()) {

            if ((entity = serverWorld.getEntity(uuid)) != null) {
                return entity;
            }

        }

        return null;

    }

    public static BlockState getInWallBlockState(Entity playerEntity) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(int i = 0; i < 8; ++i) {
            double d = playerEntity.getX() + (double)(((float)((i >> 0) % 2) - 0.5F) * playerEntity.getWidth() * 0.8F);
            double e = playerEntity.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
            double f = playerEntity.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * playerEntity.getWidth() * 0.8F);
            mutable.set(d, e, f);
            BlockState blockState = playerEntity.getWorld().getBlockState(mutable);
            if (blockState.getRenderType() != BlockRenderType.INVISIBLE && blockState.shouldBlockVision(playerEntity.getWorld(), mutable)) {
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

    public static boolean allPresent(SerializableData.Instance data, String... fieldNames) {

        for (String field : fieldNames) {

            if (!data.isPresent(field)) {
                return false;
            }

        }

        return true;

    }

    public static boolean anyPresent(SerializableData.Instance data, String... fields) {

        for (String field : fields) {

            if (data.isPresent(field)) {
                return true;
            }

        }

        return false;

    }

    public static boolean hasSpaceInInventory(PlayerEntity player, ItemStack stack) {
        PlayerInventory inventory = player.getInventory();
        return inventory.getOccupiedSlotWithRoomForStack(stack) != -1
            || inventory.getEmptySlot() != -1;
    }

    public static <E, C extends Collection<E>> BinaryOperator<C> mergeCollections() {
        return (coll1, coll2) -> {
            coll1.addAll(coll2);
            return coll1;
        };
    }

}
