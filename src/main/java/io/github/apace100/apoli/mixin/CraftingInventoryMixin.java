package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.power.Power;
import net.minecraft.inventory.CraftingInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CraftingInventory.class)
public class CraftingInventoryMixin implements PowerCraftingInventory {

    @Unique
    private Power apoli$CachedPower;

    @Override
    public void apoli$setPower(Power power) {
        apoli$CachedPower = power;
    }

    @Override
    public Power apoli$getPower() {
        return apoli$CachedPower;
    }
}
