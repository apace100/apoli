package io.github.apace100.apoli.access;

import net.minecraft.util.Identifier;

import java.util.Optional;

public interface SlotState {
    Optional<Identifier> apoli$getState();
    void apoli$setState(Identifier state);
}
