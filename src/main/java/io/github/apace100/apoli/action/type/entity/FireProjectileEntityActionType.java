package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.Optional;

/**
 *  TODO: Add a {@code bientity_action} field -eggohito
 */
public class FireProjectileEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<FireProjectileEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("entity_type", SerializableDataTypes.ENTITY_TYPE)
            .add("projectile_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("tag", SerializableDataTypes.NBT_COMPOUND, new NbtCompound())
            .add("divergence", SerializableDataTypes.FLOAT, 1.0F)
            .add("speed", SerializableDataTypes.FLOAT, 1.5F)
            .add("count", SerializableDataTypes.INT, 1),
        data -> new FireProjectileEntityActionType(
            data.get("entity_type"),
            data.get("projectile_action"),
            data.get("tag"),
            data.get("divergence"),
            data.get("speed"),
            data.get("count")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("entity_type", actionType.entityType)
            .set("projectile_action", actionType.projectileAction)
            .set("tag", actionType.tag)
            .set("divergence", actionType.divergence)
            .set("speed", actionType.speed)
            .set("count", actionType.count)
    );

    private final EntityType<?> entityType;
    private final Optional<EntityAction> projectileAction;

    private final NbtCompound tag;

    private final float divergence;
    private final float speed;

    private final int count;

    public FireProjectileEntityActionType(EntityType<?> entityType, Optional<EntityAction> projectileAction, NbtCompound tag, float divergence, float speed, int count) {
        this.entityType = entityType;
        this.projectileAction = projectileAction;
        this.tag = tag;
        this.divergence = divergence;
        this.speed = speed;
        this.count = count;
    }

    @Override
    protected void execute(Entity entity) {

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        Random random = serverWorld.getRandom();

        Vec3d velocity = entity.getVelocity();
        Vec3d verticalOffset = entity.getPos().add(0, entity.getEyeHeight(entity.getPose()), 0);

        float pitch = entity.getPitch();
        float yaw = entity.getYaw();

        for (int i = 0; i < count; i++) {

            Entity entityToSpawn = MiscUtil
                .getEntityWithPassengers(serverWorld, entityType, tag, verticalOffset, yaw, pitch)
                .orElse(null);

            if (entityToSpawn == null) {
                return;
            }

            if (entityToSpawn instanceof ProjectileEntity projectileToSpawn) {

                if (projectileToSpawn instanceof ExplosiveProjectileEntity explosiveProjectileToSpawn) {
                    explosiveProjectileToSpawn.accelerationPower = speed;
                }

                projectileToSpawn.setOwner(entity);
                projectileToSpawn.setVelocity(entity, pitch, yaw, 0F, speed, divergence);

            }

            else {

                float j = 0.017453292F;
                double k = 0.007499999832361937D;

                float l = -MathHelper.sin(yaw * j) * MathHelper.cos(pitch * j);
                float m = -MathHelper.sin(pitch * j);
                float n =  MathHelper.cos(yaw * j) * MathHelper.cos(pitch * j);

                Vec3d velocityToApply = new Vec3d(l, m, n)
                    .normalize()
                    .add(random.nextGaussian() * k * divergence, random.nextGaussian() * k * divergence, random.nextGaussian() * k * divergence)
                    .multiply(speed);

                entityToSpawn.setVelocity(velocityToApply);
                entityToSpawn.addVelocity(velocity.x, entity.isOnGround() ? 0.0D : velocity.y, velocity.z);

            }

            if (!tag.isEmpty()) {

                NbtCompound mergedNbt = entityToSpawn.writeNbt(new NbtCompound());
                mergedNbt.copyFrom(tag);

                entityToSpawn.readNbt(mergedNbt);

            }

            serverWorld.spawnNewEntityAndPassengers(entityToSpawn);
            projectileAction.ifPresent(action -> action.execute(entityToSpawn));

        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.FIRE_PROJECTILE;
    }

}
