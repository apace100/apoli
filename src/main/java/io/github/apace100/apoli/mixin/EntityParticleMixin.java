package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ParticlePower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public abstract class EntityParticleMixin extends Entity {

    public EntityParticleMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void tick(CallbackInfo info) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player != null) {
            boolean firstPerson = MinecraftClient.getInstance().options.getPerspective().isFirstPerson();
            PowerHolderComponent component = PowerHolderComponent.KEY.get(this);
            List<ParticlePower> particlePowers = component.getPowers(ParticlePower.class);
            for (ParticlePower particlePower : particlePowers) {
                if(!this.isInvisibleTo(player) || particlePower.isVisibleWhileInvisible()) {
                    if (((Object) this != player || (!firstPerson || particlePower.isVisibleInFirstPerson()))) {
                        if (this.age % particlePower.getFrequency() == 0 && particlePower.getCount() > 0 && particlePower.getSpeed() >= 0) {
                            Vec3d spread = particlePower.getSpread();
                            for (int i = 0; i < particlePower.getCount(); i++) {
                                getWorld().addParticle(particlePower.getParticle(), this.getX() + this.random.nextGaussian() * spread.getX(), particlePower.getOffset_y() + this.getY() + this.random.nextGaussian() * spread.getY(), this.getZ() + this.random.nextGaussian() * spread.getZ(), (2.0 * this.random.nextDouble() - 1.0) * particlePower.getSpeed(), (2.0 * this.random.nextDouble() - 1.0) *particlePower.getSpeed(), (2.0 * this.random.nextDouble() - 1.0) *particlePower.getSpeed());
                            }
                        }
                    }
                }
            }
        }
    }
}