package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.access.PowerLinkedListenerData;
import io.github.apace100.apoli.power.GameEventListenerPower;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.event.Vibrations;
import net.minecraft.world.event.listener.Vibration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Vibrations.Ticker.class)
public interface VibrationsTickerMixin {

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/event/Vibrations$Ticker;tryListen(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/event/Vibrations$ListenerData;Lnet/minecraft/world/event/Vibrations$Callback;)V"))
    private static void apoli$tryListenWithoutParticles(ServerWorld world, Vibrations.ListenerData listenerData, Vibrations.Callback callback, Operation<Void> original) {

        Vibration vibration = listenerData.getSelector().getVibrationToTick(world.getTime()).orElse(null);
        if (vibration == null) {
            return;
        }

        if (((PowerLinkedListenerData) listenerData).apoli$getPower().map(GameEventListenerPower::shouldShowParticle).orElse(true)) {
            original.call(world, listenerData, callback);
            return;
        }

        listenerData.setVibration(vibration);
        listenerData.setDelay(callback.getDelay(vibration.distance()));

        callback.onListen();
        listenerData.getSelector().clear();

    }

    @WrapOperation(method = "spawnVibrationParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnParticles(Lnet/minecraft/particle/ParticleEffect;DDDIDDDD)I"))
    private static <T extends ParticleEffect> int apoli$preventSpawningParticles(ServerWorld instance, T particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed, Operation<Integer> original, ServerWorld world, Vibrations.ListenerData listenerData, Vibrations.Callback callback) {
        return ((PowerLinkedListenerData) listenerData).apoli$getPower().map(GameEventListenerPower::shouldShowParticle).orElse(true) ? original.call(instance, particle, x, y, z, count, deltaX, deltaY, deltaZ, speed) : 0;
    }

}
