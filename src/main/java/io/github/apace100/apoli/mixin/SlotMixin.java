package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.SlotState;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(Slot.class)
public abstract class SlotMixin implements SlotState {

    @Unique
    private Identifier apoli$state;

    @Override
    public Optional<Identifier> apoli$getState() {
        return Optional.ofNullable(apoli$state);
    }

    @Override
    public void apoli$setState(Identifier state) {
        this.apoli$state = state;
    }

}
