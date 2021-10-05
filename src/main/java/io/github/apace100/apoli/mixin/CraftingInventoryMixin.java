package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.power.Power;
import net.minecraft.inventory.CraftingInventory;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CraftingInventory.class)
public class CraftingInventoryMixin implements PowerCraftingInventory {

    private Power apoli$CachedPower;

    @Override
    public void setPower(Power power) {
        apoli$CachedPower = power;
    }

    @Override
    public Power getPower() {
        return apoli$CachedPower;
    }
}
