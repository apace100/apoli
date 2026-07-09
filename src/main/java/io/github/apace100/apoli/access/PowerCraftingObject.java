package io.github.apace100.apoli.access;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface PowerCraftingObject {

    @Nullable
    PlayerEntity apoli$getPlayer();

    void apoli$setPlayer(@Nullable PlayerEntity player);

}
