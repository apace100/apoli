package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
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

import java.util.function.Consumer;

public class FireProjectileAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        EntityType<?> entityType = data.get("entity_type");
        NbtCompound entityNbt = data.get("tag");
        Random random = serverWorld.getRandom();

        Vec3d rotationVector = entity.getRotationVector();
        Vec3d velocity = entity.getVelocity();
        Vec3d verticalOffset = entity
            .getPos()
            .add(0, entity.getEyeHeight(entity.getPose()), 0);

        float divergence = data.get("divergence");
        float speed = data.get("speed");
        float pitch = entity.getPitch();
        float yaw = entity.getYaw();

        int count = data.get("count");
        for (int i = 0; i < count; i++) {

            Entity entityToSpawn = MiscUtil
                .getEntityWithPassengers(serverWorld, entityType, entityNbt, verticalOffset, yaw, pitch)
                .orElse(null);

            if (entityToSpawn == null) {
                return;
            }

            if (entityToSpawn instanceof ProjectileEntity projectileToSpawn) {

                if (projectileToSpawn instanceof ExplosiveProjectileEntity explosiveProjectileToSpawn) {
                    explosiveProjectileToSpawn.powerX = rotationVector.x * speed;
                    explosiveProjectileToSpawn.powerY = rotationVector.y * speed;
                    explosiveProjectileToSpawn.powerZ = rotationVector.z * speed;
                }

                projectileToSpawn.setOwner(entity);
                projectileToSpawn.setVelocity(entity, pitch, yaw, 0F, speed, divergence);

            } else {

                float j = 0.017453292F;
                double k = 0.007499999832361937D;

                float l = -MathHelper.sin(yaw * j) * MathHelper.cos(pitch * j);
                float m = -MathHelper.sin(pitch * j);
                float n =  MathHelper.cos(yaw * j) * MathHelper.cos(pitch * j);

                Vec3d entityToSpawnVelocity = new Vec3d(l, m, n)
                    .normalize()
                    .add(random.nextGaussian() * k * divergence, random.nextGaussian() * k * divergence, random.nextGaussian() * k * divergence)
                    .multiply(speed);

                entityToSpawn.setVelocity(entityToSpawnVelocity);
                entityToSpawn.addVelocity(velocity.x, entity.isOnGround() ? 0.0D : velocity.y, velocity.z);

            }

            if (!entityNbt.isEmpty()) {

                NbtCompound mergedNbt = entityToSpawn.writeNbt(new NbtCompound());
                mergedNbt.copyFrom(entityNbt);

                entityToSpawn.readNbt(mergedNbt);

            }

            serverWorld.spawnNewEntityAndPassengers(entityToSpawn);
            data.<Consumer<Entity>>ifPresent("projectile_action", projectileAction -> projectileAction.accept(entityToSpawn));

        }

    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("fire_projectile"),
            new SerializableData()
                .add("entity_type", SerializableDataTypes.ENTITY_TYPE)
                .add("divergence", SerializableDataTypes.FLOAT, 1F)
                .add("speed", SerializableDataTypes.FLOAT, 1.5F)
                .add("count", SerializableDataTypes.INT, 1)
                .add("tag", SerializableDataTypes.NBT, new NbtCompound())
                .add("projectile_action", ApoliDataTypes.ENTITY_ACTION, null),
            FireProjectileAction::action
        );
    }

}
