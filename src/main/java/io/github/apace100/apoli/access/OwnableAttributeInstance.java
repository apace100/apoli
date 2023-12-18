package io.github.apace100.apoli.access;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface OwnableAttributeInstance {
    @Nullable Entity apoli$getOwner();
    void apoli$setOwner(Entity owner);
}
