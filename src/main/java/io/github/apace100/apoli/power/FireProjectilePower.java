package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtLong;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.Optional;
import java.util.function.Consumer;

public class FireProjectilePower extends ActiveCooldownPower {

    private final EntityType<?> entityType;
    private final int projectileCount;
    private final int interval;
    private final int startDelay;
    private final float speed;
    private final float divergence;
    private final SoundEvent soundEvent;
    private final NbtCompound tag;
    private final Consumer<Entity> projectileAction;
    private final Consumer<Entity> shooterAction;

    private boolean isFiringProjectiles;
    private boolean finishedStartDelay;
    private int shotProjectiles;

    public FireProjectilePower(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender, EntityType<?> entityType, int projectileCount, int interval, int startDelay, float speed, float divergence, SoundEvent soundEvent, NbtCompound tag, Consumer<Entity> projectileAction, Consumer<Entity> shooterAction) {
        super(type, entity, cooldownDuration, hudRender, null);
        this.entityType = entityType;
        this.projectileCount = projectileCount;
        this.interval = interval;
        this.startDelay = startDelay;
        this.speed = speed;
        this.divergence = divergence;
        this.soundEvent = soundEvent;
        this.tag = tag;
        this.projectileAction = projectileAction;
        this.shooterAction = shooterAction;
        this.setTicking(true);
    }

    @Override
    public void onUse() {
        if(canUse()) {
            isFiringProjectiles = true;
            use();
        }
    }

    @Override
    public NbtElement toTag() {
        NbtCompound nbt = new NbtCompound();
        nbt.putLong("LastUseTime", lastUseTime);
        nbt.putInt("ShotProjectiles", shotProjectiles);
        nbt.putBoolean("FinishedStartDelay", finishedStartDelay);
        nbt.putBoolean("IsFiringProjectiles", isFiringProjectiles);
        return nbt;
    }

    @Override
    public void fromTag(NbtElement tag) {
        if(tag instanceof NbtLong) {
            lastUseTime = ((NbtLong)tag).longValue();
        }
        else {
            lastUseTime = ((NbtCompound)tag).getLong("LastUseTime");
            shotProjectiles = ((NbtCompound)tag).getInt("ShotProjectiles");
            finishedStartDelay = ((NbtCompound)tag).getBoolean("FinishedStartDelay");
            isFiringProjectiles = ((NbtCompound)tag).getBoolean("IsFiringProjectiles");
        }
    }

    public void tick() {
        if(isFiringProjectiles) {
            if(!finishedStartDelay && startDelay == 0) {
                finishedStartDelay = true;
            }
            if(!finishedStartDelay && (entity.getEntityWorld().getTime() - lastUseTime) % startDelay == 0) {
                finishedStartDelay = true;
                shotProjectiles += 1;
                if(shotProjectiles <= projectileCount) {
                    if(soundEvent != null) {
                        entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundEvent, SoundCategory.NEUTRAL, 0.5F, 0.4F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
                    }
                    if(!entity.getWorld().isClient) {
                        fireProjectile();
                    }
                }
                else {
                    shotProjectiles = 0;
                    finishedStartDelay = false;
                    isFiringProjectiles = false;
                }
            }
            else if(interval == 0 && finishedStartDelay) {
                if(soundEvent != null) {
                    entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundEvent, SoundCategory.NEUTRAL, 0.5F, 0.4F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
                }
                if(!entity.getWorld().isClient) {
                    for(; shotProjectiles < projectileCount; shotProjectiles++) {
                        fireProjectile();
                    }
                }
                shotProjectiles = 0;
                finishedStartDelay = false;
                isFiringProjectiles = false;
            }
            else if (finishedStartDelay && (entity.getEntityWorld().getTime() - lastUseTime) % interval == 0) {
                shotProjectiles += 1;
                if(shotProjectiles <= projectileCount) {
                    if(soundEvent != null) {
                        entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), soundEvent, SoundCategory.NEUTRAL, 0.5F, 0.4F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
                    }
                    if(!entity.getWorld().isClient) {
                        fireProjectile();
                    }
                }
                else {
                    shotProjectiles = 0;
                    finishedStartDelay = false;
                    isFiringProjectiles = false;
                }
            }
        }
    }

    private void fireProjectile() {

        if (entityType == null || entity.getWorld().isClient) return;

        ServerWorld serverWorld = (ServerWorld) entity.getWorld();
        float yaw = entity.getYaw();
        float pitch = entity.getPitch();

        Optional<Entity> opt$entityToSpawn = MiscUtil.getEntityWithPassengers(
            serverWorld,
            entityType,
            tag,
            entity.getPos().add(0, entity.getEyeHeight(entity.getPose()), 0),
            yaw,
            pitch
        );

        if (opt$entityToSpawn.isEmpty()) return;

        Vec3d rotationVector = entity.getRotationVector();
        Vec3d velocity = entity.getVelocity();
        Entity entityToSpawn = opt$entityToSpawn.get();
        Random random = serverWorld.getRandom();

        if (entityToSpawn instanceof ProjectileEntity projectileToSpawn) {

            if (projectileToSpawn instanceof ExplosiveProjectileEntity explosiveProjectileToSpawn) {
                explosiveProjectileToSpawn.powerX = rotationVector.x * speed;
                explosiveProjectileToSpawn.powerY = rotationVector.y * speed;
                explosiveProjectileToSpawn.powerZ = rotationVector.z * speed;
            }

            projectileToSpawn.setOwner(entity);
            projectileToSpawn.setVelocity(entity, pitch, yaw, 0F, speed, divergence);

        }

        else {

            float  f = 0.017453292F;
            double g = 0.007499999832361937D;

            float h = -MathHelper.sin(yaw * f) * MathHelper.cos(pitch * f);
            float i = -MathHelper.sin(pitch * f);
            float j =  MathHelper.cos(yaw * f) * MathHelper.cos(pitch * f);

            Vec3d vec3d = new Vec3d(h, i, j)
                .normalize()
                .add(random.nextGaussian() * g * divergence, random.nextGaussian() * g * divergence, random.nextGaussian() * g * divergence)
                .multiply(speed);

            entityToSpawn.setVelocity(vec3d);
            entityToSpawn.addVelocity(velocity.x, entity.isOnGround() ? 0.0D : velocity.y, velocity.z);

        }

        serverWorld.spawnNewEntityAndPassengers(entityToSpawn);
        if (projectileAction != null) projectileAction.accept(entityToSpawn);
        if (shooterAction != null) shooterAction.accept(entity);

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("fire_projectile"),
            new SerializableData()
                .add("cooldown", SerializableDataTypes.INT, 1)
                .add("count", SerializableDataTypes.INT, 1)
                .add("interval", SerializableDataTypes.INT, 0)
                .add("start_delay", SerializableDataTypes.INT, 0)
                .add("speed", SerializableDataTypes.FLOAT, 1.5F)
                .add("divergence", SerializableDataTypes.FLOAT, 1F)
                .add("sound", SerializableDataTypes.SOUND_EVENT, null)
                .add("entity_type", SerializableDataTypes.ENTITY_TYPE)
                .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                .add("tag", SerializableDataTypes.NBT, null)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key())
                .add("projectile_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("shooter_action", ApoliDataTypes.ENTITY_ACTION, null),
            data -> (powerType, livingEntity) -> {

                FireProjectilePower fpp = new FireProjectilePower(
                    powerType,
                    livingEntity,
                    data.get("cooldown"),
                    data.get("hud_render"),
                    data.get("entity_type"),
                    data.get("count"),
                    data.get("interval"),
                    data.get("start_delay"),
                    data.get("speed"),
                    data.get("divergence"),
                    data.get("sound"),
                    data.get("tag"),
                    data.get("projectile_action"),
                    data.get("shooter_action")
                );

                fpp.setKey(data.get("key"));
                return fpp;

            }
        ).allowCondition();
    }
}
