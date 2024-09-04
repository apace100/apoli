package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.power.type.GameEventListenerPowerType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.Vibrations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Vibrations.class)
public interface VibrationsMixin {

	@Mixin(Vibrations.Callback.class)
	interface CallbackMixin {

		@WrapOperation(method = "canAccept", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/entry/RegistryEntry;isIn(Lnet/minecraft/registry/tag/TagKey;)Z", ordinal = 0))
		private boolean apoli$accountForPowerCallbacks(RegistryEntry<GameEvent> gameEvent, TagKey<GameEvent> gameEventTag, Operation<Boolean> original) {
            return (Vibrations.Callback) this instanceof GameEventListenerPowerType.Callback powerCallback
				? powerCallback.shouldAccept(gameEvent)
				: original.call(gameEvent, gameEventTag);
		}

	}

	@Mixin(Vibrations.Ticker.class)
	interface TickerMixin {

		@WrapWithCondition(method = "method_51408", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnParticles(Lnet/minecraft/particle/ParticleEffect;DDDIDDDD)I"))
		private static boolean apoli$onlyShowParticleWhenSpecified(ServerWorld world, ParticleEffect particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed, Vibrations.ListenerData listenerData) {
			return !(listenerData instanceof GameEventListenerPowerType.ListenerData powerListenerData)
				|| powerListenerData.shouldShowParticle();
		}

	}

}
