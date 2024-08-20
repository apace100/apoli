package io.github.apace100.apoli.access;

import io.github.apace100.apoli.power.type.PowerType;
import net.minecraft.entity.player.PlayerEntity;

public interface PowerCraftingInventory {

    void apoli$setPowerType(PowerType powerType);
    PowerType apoli$getPowerType();

    void apoli$setPlayer(PlayerEntity player);
    PlayerEntity apoli$getPlayer();

}
