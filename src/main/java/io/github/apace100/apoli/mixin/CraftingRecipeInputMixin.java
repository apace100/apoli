package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.power.Power;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.input.CraftingRecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CraftingRecipeInput.class)
public abstract class CraftingRecipeInputMixin implements PowerCraftingInventory {

    @Unique
    private Power apoli$cachedPower;

    @Unique
    private PlayerEntity apoli$cachedPlayer;

    @Override
    public Power apoli$getPower() {
        return apoli$cachedPower;
    }

    @Override
    public void apoli$setPower(Power power) {
        this.apoli$cachedPower = power;
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
