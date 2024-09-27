package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.power.type.PowerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collection;
import java.util.LinkedList;

@Mixin(CraftingRecipeInput.class)
public abstract class CraftingRecipeInputMixin implements PowerCraftingInventory {

    @Unique
    private Collection<? extends PowerType> apoli$cachedPowerTypes = new LinkedList<>();

    @Unique
    private PlayerEntity apoli$cachedPlayer;

    @Unique
    private CraftingInventory apoli$inventory;

    @Override
    public Collection<? extends PowerType> apoli$getPowerTypes() {
        return apoli$cachedPowerTypes;
    }

    @Override
    public void apoli$setPowerTypes(Collection<? extends PowerType> powerType) {

        this.apoli$cachedPowerTypes = powerType;

        if (this.apoli$getInventory() instanceof PowerCraftingInventory pci) {
            pci.apoli$setPowerTypes(this.apoli$getPowerTypes());
        }

    }

    @Override
    public PlayerEntity apoli$getPlayer() {
        return apoli$cachedPlayer;
    }

    @Override
    public void apoli$setPlayer(PlayerEntity player) {

        this.apoli$cachedPlayer = player;

        if (this.apoli$getInventory() instanceof PowerCraftingInventory pci) {
            pci.apoli$setPlayer(this.apoli$getPlayer());
        }

    }

    @Override
    public CraftingInventory apoli$getInventory() {
        return apoli$inventory;
    }

    @Override
    public void apoli$setInventory(CraftingInventory inventory) {
        this.apoli$inventory = inventory;
    }

}
