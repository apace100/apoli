package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerLinkedListenerData;
import io.github.apace100.apoli.power.type.GameEventListenerPowerType;
import net.minecraft.world.event.Vibrations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(Vibrations.ListenerData.class)
public abstract class VibrationsListenerDataMixin implements PowerLinkedListenerData {

    @Unique
    private GameEventListenerPowerType apoli$linkedPower;

    @Override
    public Optional<GameEventListenerPowerType> apoli$getPower() {
        return Optional.ofNullable(apoli$linkedPower);
    }

    @Override
    public void apoli$setPower(GameEventListenerPowerType power) {
        apoli$linkedPower = power;
    }

}
