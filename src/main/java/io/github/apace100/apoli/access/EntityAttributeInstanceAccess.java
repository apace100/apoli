package io.github.apace100.apoli.access;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface EntityAttributeInstanceAccess {
    void apoli$setEntity(Entity entity);
    @Nullable Entity apoli$getEntity();
}
