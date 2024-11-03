package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
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
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

//  TODO: Remove this power type in favor of using its action type counterpart -eggohito
@Deprecated
public class FireProjectilePowerType extends ActiveCooldownPowerType {

    public static final TypedDataObjectFactory<FireProjectilePowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("projectile_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("shooter_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("entity_type", SerializableDataTypes.ENTITY_TYPE)
            .add("tag", SerializableDataTypes.NBT_COMPOUND, new NbtCompound())
            .add("sound", SerializableDataTypes.SOUND_EVENT.optional(), Optional.empty())
            .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Key())
            .add("hud_render", HudRender.DATA_TYPE, HudRender.DONT_RENDER)
            .add("cooldown", SerializableDataTypes.INT, 1)
            .add("count", SerializableDataTypes.INT, 1)
            .add("interval", SerializableDataTypes.NON_NEGATIVE_INT, 0)
            .add("start_delay", SerializableDataTypes.NON_NEGATIVE_INT, 0)
            .add("speed", SerializableDataTypes.FLOAT, 1.5F)
            .add("divergence", SerializableDataTypes.FLOAT, 1.0F),
        (data, condition) -> new FireProjectilePowerType(
            data.get("projectile_action"),
            data.get("shooter_action"),
            data.get("entity_type"),
            data.get("tag"),
            data.get("sound"),
            data.get("key"),
            data.get("hud_render"),
            data.get("cooldown"),
            data.get("count"),
            data.get("interval"),
            data.get("start_delay"),
            data.get("speed"),
            data.get("divergence"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("projectile_action", powerType.projectileAction)
            .set("shooter_action", powerType.shooterAction)
            .set("entity_type", powerType.entityType)
            .set("tag", powerType.tag)
            .set("sound", powerType.soundEvent)
            .set("key", powerType.getKey())
            .set("hud_render", powerType.getRenderSettings())
            .set("cooldown", powerType.getCooldown())
            .set("count", powerType.projectileCount)
            .set("interval", powerType.interval)
            .set("start_delay", powerType.startDelay)
            .set("speed", powerType.speed)
            .set("divergence", powerType.divergence)
    );

    private final Optional<EntityAction> projectileAction;
    private final Optional<EntityAction> shooterAction;

    private final EntityType<?> entityType;
    private final NbtCompound tag;

    private final Optional<SoundEvent> soundEvent;

    private final int projectileCount;
    private final int interval;
    private final int startDelay;

    private final float speed;
    private final float divergence;

    private boolean isFiringProjectiles;
    private boolean finishedStartDelay;
    private int shotProjectiles;

    public FireProjectilePowerType(Optional<EntityAction> projectileAction, Optional<EntityAction> shooterAction, EntityType<?> entityType, NbtCompound tag, Optional<SoundEvent> soundEvent, Key key, HudRender hudRender, int cooldownDuration, int projectileCount, int interval, int startDelay, float speed, float divergence, Optional<EntityCondition> condition) {
        super(hudRender, cooldownDuration, key, condition);
        this.projectileAction = projectileAction;
        this.shooterAction = shooterAction;
        this.entityType = entityType;
        this.tag = tag;
        this.soundEvent = soundEvent;
        this.projectileCount = projectileCount;
        this.interval = interval;
        this.startDelay = startDelay;
        this.speed = speed;
        this.divergence = divergence;
        this.setTicking(true);
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.FIRE_PROJECTILE;
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

        if (tag instanceof NbtLong nbtLong) {
            this.lastUseTime = nbtLong.longValue();
        }

        else if (tag instanceof NbtCompound nbtCompound) {
            this.lastUseTime = nbtCompound.getLong("LastUseTime");
            this.shotProjectiles = nbtCompound.getInt("ShotProjectiles");
            this.finishedStartDelay = nbtCompound.getBoolean("FinishedStartDelay");
            this.isFiringProjectiles = nbtCompound.getBoolean("IsFiringProjectiles");
        }

    }

    public void serverTick() {

        LivingEntity holder = getHolder();

        if (isFiringProjectiles) {

            if (!finishedStartDelay && startDelay == 0) {
                finishedStartDelay = true;
            }

            if (!finishedStartDelay && (holder.getEntityWorld().getTime() - lastUseTime) % startDelay == 0) {

                this.finishedStartDelay = true;
                this.shotProjectiles++;

                if (shotProjectiles <= projectileCount) {

					soundEvent.ifPresent(event -> holder.getWorld().playSound(null, holder.getX(), holder.getY(), holder.getZ(), event, SoundCategory.NEUTRAL, 0.5F, 0.4F / (holder.getRandom().nextFloat() * 0.4F + 0.8F)));

                    if (!holder.getWorld().isClient()) {
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

				soundEvent.ifPresent(event -> holder.getWorld().playSound(null, holder.getX(), holder.getY(), holder.getZ(), event, SoundCategory.NEUTRAL, 0.5F, 0.4F / (holder.getRandom().nextFloat() * 0.4F + 0.8F)));

                if (!holder.getWorld().isClient()) {

                    for(; shotProjectiles < projectileCount; shotProjectiles++) {
                        fireProjectile();
                    }

                }

                this.shotProjectiles = 0;
                this.finishedStartDelay = false;
                this.isFiringProjectiles = false;

            }

            else if (finishedStartDelay && (holder.getEntityWorld().getTime() - lastUseTime) % interval == 0) {

                this.shotProjectiles++;

                if (shotProjectiles <= projectileCount) {

					soundEvent.ifPresent(event -> holder.getWorld().playSound(null, holder.getX(), holder.getY(), holder.getZ(), event, SoundCategory.NEUTRAL, 0.5F, 0.4F / (holder.getRandom().nextFloat() * 0.4F + 0.8F)));

                    if (!holder.getWorld().isClient) {
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

        LivingEntity holder = getHolder();
        if (!(holder.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        Random random = serverWorld.getRandom();

        Vec3d velocity = holder.getVelocity();
        Vec3d verticalOffset = holder.getPos().add(0, holder.getEyeHeight(holder.getPose()), 0);

        float pitch = holder.getPitch();
        float yaw = holder.getYaw();

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

            projectileToSpawn.setOwner(holder);
            projectileToSpawn.setVelocity(holder, pitch, yaw, 0F, speed, divergence);

        }

        else {

            float  j = 0.017453292F;
            double k = 0.007499999832361937D;

            float l = -MathHelper.sin(yaw * j) * MathHelper.cos(pitch * j);
            float m = -MathHelper.sin(pitch * j);
            float n =  MathHelper.cos(yaw * j) * MathHelper.cos(pitch * j);

            Vec3d velocityToApply = new Vec3d(l, m, n)
                .normalize()
                .add(random.nextGaussian() * k * divergence, random.nextGaussian() * k * divergence, random.nextGaussian() * k * divergence)
                .multiply(speed);

            entityToSpawn.setVelocity(velocityToApply);
            entityToSpawn.addVelocity(velocity.x, holder.isOnGround() ? 0.0D : velocity.y, velocity.z);

        }

        if (!tag.isEmpty()) {

            NbtCompound mergedTag = entityToSpawn.writeNbt(new NbtCompound());
            mergedTag.copyFrom(tag);

            entityToSpawn.readNbt(mergedTag);

        }

        serverWorld.spawnNewEntityAndPassengers(entityToSpawn);

        projectileAction.ifPresent(action -> action.execute(entityToSpawn));
        shooterAction.ifPresent(action -> action.execute(holder));

    }

}
