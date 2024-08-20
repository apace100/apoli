package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.power.type.PowerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.input.CraftingRecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CraftingRecipeInput.class)
public abstract class CraftingRecipeInputMixin implements PowerCraftingInventory {

    @Unique
    private PowerType apoli$cachedPowerType;

    @Unique
    private PlayerEntity apoli$cachedPlayer;

    @Override
    public PowerType apoli$getPowerType() {
        return apoli$cachedPowerType;
    }

    @Override
    public void apoli$setPowerType(PowerType powerType) {
        this.apoli$cachedPowerType = powerType;
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
