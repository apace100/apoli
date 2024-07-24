package io.github.apace100.apoli.access;

import io.github.apace100.apoli.power.Power;
import net.minecraft.entity.player.PlayerEntity;

public interface PowerCraftingInventory {

    void apoli$setPower(Power power);
    Power apoli$getPower();

    void apoli$setPlayer(PlayerEntity player);
    PlayerEntity apoli$getPlayer();

}
