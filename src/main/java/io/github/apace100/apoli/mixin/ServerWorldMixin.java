package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PreventGameEventPower;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @Inject(method = "emitGameEvent", at = @At("HEAD"), cancellable = true)
    private void preventGameEventEmission(GameEvent event, Vec3d emitterPos, GameEvent.Emitter emitter, CallbackInfo ci) {
        if(emitter.sourceEntity() != null) {
            Entity entity = emitter.sourceEntity();
            List<PreventGameEventPower> preventingPowers = PowerHolderComponent.getPowers(entity, PreventGameEventPower.class).stream().filter(p -> p.doesPrevent(event)).toList();
            if(preventingPowers.size() > 0) {
                preventingPowers.forEach(p -> p.executeAction(entity));
                ci.cancel();
            }
        }
    }
}
