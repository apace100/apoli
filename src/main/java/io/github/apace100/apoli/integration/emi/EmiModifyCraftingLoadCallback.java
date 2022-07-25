package io.github.apace100.apoli.integration.emi;

import dev.emi.emi.api.EmiRegistry;
import io.github.apace100.apoli.power.PowerType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.text.MutableText;

import javax.annotation.Nullable;

/**
 * This callback is fired at the end of loading each recipe that is loaded through
 *  a ModifyCraftingPower if the recipe is not any vanilla recipe types that are supported by EMI.
 * Use this callback to register your own crafting table recipes that are modified by ModifyCrafting.
 *
 * Make sure to return true so other recipes aren't registered.
 */
public interface EmiModifyCraftingLoadCallback {

    Event<EmiModifyCraftingLoadCallback> EVENT = EventFactory.createArrayBacked(EmiModifyCraftingLoadCallback.class,
            (listeners) -> (registry, recipe, powerType, multiplePowerTypeText) -> {
            for (EmiModifyCraftingLoadCallback event : listeners) {
                if (event.onEmiModifyCraftingLoad(registry, recipe, powerType, multiplePowerTypeText)) break;
            }
            return true;
        }
    );

    boolean onEmiModifyCraftingLoad(EmiRegistry registry, Recipe<CraftingInventory> recipe, PowerType<?> powerType, @Nullable MutableText multiplePowerTypeName);
}
