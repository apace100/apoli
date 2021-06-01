package io.github.apace100.apoli.power;

import io.github.apace100.apoli.mixin.EyeHeightAccess;
import io.github.apace100.apoli.util.HudRender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class FireProjectilePower extends ActiveCooldownPower {

    private final EntityType entityType;
    private final int projectileCount;
    private final float speed;
    private final float divergence;
    private final SoundEvent soundEvent;
    private final NbtCompound tag;

    public FireProjectilePower(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender, EntityType entityType, int projectileCount, float speed, float divergence, SoundEvent soundEvent, NbtCompound tag) {
        super(type, entity, cooldownDuration, hudRender, null);
        this.entityType = entityType;
        this.projectileCount = projectileCount;
        this.speed = speed;
        this.divergence = divergence;
        this.soundEvent = soundEvent;
        this.tag = tag;
    }

    @Override
    public void onUse() {
        if(canUse()) {
            fireProjectiles();
            use();
        }
    }

    private void fireProjectiles() {
        if(soundEvent != null) {
            entity.world.playSound((PlayerEntity)null, entity.getX(), entity.getY(), entity.getZ(), soundEvent, SoundCategory.NEUTRAL, 0.5F, 0.4F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
        }
        if (!entity.world.isClient) {
            for(int i = 0; i < projectileCount; i++) {
                fireProjectile();
            }
        }
    }

    private void fireProjectile() {
        if(entityType != null) {
            Entity entity = entityType.create(this.entity.world);
            if(entity == null) {
                return;
            }
            Vec3d rotationVector = this.entity.getRotationVector();
            float yaw = this.entity.getYaw();
            float pitch = this.entity.getPitch();
            Vec3d spawnPos = this.entity.getPos().add(0, ((EyeHeightAccess) this.entity).callGetEyeHeight(this.entity.getPose(), this.entity.getDimensions(this.entity.getPose())), 0).add(rotationVector);
            entity.refreshPositionAndAngles(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), pitch, yaw);
            if(entity instanceof ProjectileEntity) {
                if(entity instanceof ExplosiveProjectileEntity) {
                    ExplosiveProjectileEntity explosiveProjectileEntity = (ExplosiveProjectileEntity)entity;
                    explosiveProjectileEntity.setPos(rotationVector.x * speed, rotationVector.y * speed, rotationVector.z * speed);
                }
                ProjectileEntity projectile = (ProjectileEntity)entity;
                projectile.setOwner(this.entity);
                projectile.setProperties(this.entity, pitch, yaw, 0F, speed, divergence);
            } else {
                float f = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
                float g = -MathHelper.sin(pitch * 0.017453292F);
                float h = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
                Vec3d vec3d = (new Vec3d(f, g, h)).normalize().add(this.entity.getRandom().nextGaussian() * 0.007499999832361937D * (double)divergence, this.entity.getRandom().nextGaussian() * 0.007499999832361937D * (double)divergence, this.entity.getRandom().nextGaussian() * 0.007499999832361937D * (double)divergence).multiply((double)speed);
                entity.setVelocity(vec3d);
                Vec3d entityVelo = this.entity.getVelocity();
                entity.setVelocity(entity.getVelocity().add(entityVelo.x, this.entity.isOnGround() ? 0.0D : entityVelo.y, entityVelo.z));
            }
            if(tag != null) {
                NbtCompound mergedTag = entity.writeNbt(new NbtCompound());
                mergedTag.copyFrom(tag);
                entity.readNbt(mergedTag);
            }
            this.entity.world.spawnEntity(entity);
        }
    }
}
