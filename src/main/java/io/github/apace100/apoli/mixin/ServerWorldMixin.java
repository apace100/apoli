package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.PreventGameEventPowerType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventDispatchManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @WrapWithCondition(method = "emitGameEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/event/listener/GameEventDispatchManager;dispatch(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/world/event/GameEvent$Emitter;)V"))
    private boolean apoli$prevenGameEvent(GameEventDispatchManager manager, RegistryEntry<GameEvent> event, Vec3d emitterPos, GameEvent.Emitter emitter) {
        return emitter.sourceEntity() == null
            || !PowerHolderComponent.withPowerTypes(emitter.sourceEntity(), PreventGameEventPowerType.class, p -> p.doesPrevent(event), PreventGameEventPowerType::executeAction);
    }

}
