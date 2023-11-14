package io.github.apace100.apoli.access;

import net.minecraft.util.Identifier;

import java.util.Optional;

public interface Identifiable {
    Optional<Identifier> apoli$getId();
    void apoli$setId(Identifier id);
}
