package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.power.type.PowerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CraftingInventory.class)
public abstract class CraftingInventoryMixin implements PowerCraftingInventory {

    @Unique
    private PowerType apoli$CachedPowerType;

    @Unique
    private PlayerEntity apoli$cachedPlayer;

    @Override
    public void apoli$setPowerType(PowerType powerType) {
        apoli$CachedPowerType = powerType;
    }

    @Override
    public PowerType apoli$getPowerType() {
        return apoli$CachedPowerType;
    }

    @Override
    public PlayerEntity apoli$getPlayer() {
        return apoli$cachedPlayer;
    }

    @Override
    public void apoli$setPlayer(PlayerEntity player) {
        this.apoli$cachedPlayer = player;
    }

}
