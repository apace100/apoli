package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ParticlePower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public abstract class EntityParticleMixin extends Entity {

    @Shadow public abstract EntityDimensions getDimensions(EntityPose pose);

    @Shadow protected abstract float getEyeHeight(EntityPose pose, EntityDimensions dimensions);

    public EntityParticleMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void apoli$emitParticles(CallbackInfo ci) {

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        boolean inFirstPerson = MinecraftClient.getInstance().options.getPerspective().isFirstPerson();

        if (player == null) {
            return;
        }

        double velocityX;
        double velocityY;
        double velocityZ;

        for (ParticlePower particlePower : PowerHolderComponent.getPowers(this, ParticlePower.class)) {

            if (!particlePower.doesApply(player, inFirstPerson)) {
                continue;
            }

            Vec3d spread = particlePower
                .getSpread()
                .multiply(this.getWidth(), this.getEyeHeight(this.getPose()), this.getWidth());
            Vec3d particlePos = this
                .getPos()
                .add(particlePower.getOffsetX(), particlePower.getOffsetY(), particlePower.getOffsetZ());

            if (particlePower.getCount() == 0) {

                velocityX = spread.getX() * particlePower.getSpeed();
                velocityY = spread.getY() * particlePower.getSpeed();
                velocityZ = spread.getZ() * particlePower.getSpeed();

                this.getWorld().addParticle(particlePower.getParticle(), particlePower.shouldForce(), particlePos.getX(), particlePos.getY(), particlePos.getZ(), velocityX, velocityY, velocityZ);

            } else {

                for (int i = 0; i < particlePower.getCount(); i++) {

                    Vec3d newSpread = spread.multiply(this.random.nextGaussian(), this.random.nextGaussian(), this.random.nextGaussian());
                    Vec3d newParticlePos = particlePos.add(newSpread);

                    velocityX = (2.0 * this.random.nextDouble() - 1.0) * particlePower.getSpeed();
                    velocityY = (2.0 * this.random.nextDouble() - 1.0) * particlePower.getSpeed();
                    velocityZ = (2.0 * this.random.nextDouble() - 1.0) * particlePower.getSpeed();

                    this.getWorld().addParticle(particlePower.getParticle(), particlePower.shouldForce(), newParticlePos.getX(), newParticlePos.getY(), newParticlePos.getZ(), velocityX, velocityY, velocityZ);

                }

            }

        }

    }

}