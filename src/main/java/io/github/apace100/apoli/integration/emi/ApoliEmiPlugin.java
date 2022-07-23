package io.github.apace100.apoli.integration.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.render.EmiTexture;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.*;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApoliEmiPlugin implements EmiPlugin {
    public static final EmiTexture REQUIRED_POWER_HEADING_BORDER = new EmiTexture(Apoli.identifier("textures/gui/emi_widgets.png"), 0, 0, 118, 18);
    public static final EmiTexture POWER_NAME_BORDER_MIDDLE = new EmiTexture(Apoli.identifier("textures/gui/emi_widgets.png"), 0, 0, 118, 12);
    public static final EmiTexture POWER_NAME_BORDER_BOTTOM = new EmiTexture(Apoli.identifier("textures/gui/emi_widgets.png"), 0, 0, 118, 4);
    private static final List<Identifier> SUBPOWERS = new ArrayList<>();

    @Override
    public void register(EmiRegistry registry) {
        for (PowerType<?> powerType : PowerTypeRegistry.valueStream().filter(identifierPowerTypeEntry -> identifierPowerTypeEntry instanceof MultiplePowerType<?>).toList()) {
            ((MultiplePowerType<?>) powerType).getSubPowers().forEach(subpowerId -> {
                PowerType<?> powerType2 = PowerTypeRegistry.get(subpowerId);
                Power power2 = powerType2.create(null);
                if (power2 instanceof RecipePower recipePower) {
                    addRecipePowerRecipes(registry, recipePower.getRecipe(), powerType2, (MultiplePowerType<?>)powerType);
                }
                SUBPOWERS.add(subpowerId);
            });
        }

        for (PowerType<?> powerType : PowerTypeRegistry.valueStream().filter(identifierPowerTypeEntry -> !(identifierPowerTypeEntry instanceof MultiplePowerType<?>)).toList()) {
            if (SUBPOWERS.contains(powerType.getIdentifier())) continue;
            Power power = powerType.create(null);
            if (power instanceof RecipePower recipePower) {
                addRecipePowerRecipes(registry, recipePower.getRecipe(), powerType, null);
            }
        }
    }

    private void addRecipePowerRecipes(EmiRegistry registry, Recipe<CraftingInventory> recipe, PowerType<?> powerType, @Nullable MultiplePowerType<?> multiplePowerType) {
        if (recipe instanceof ShapedRecipe) {
            registry.addRecipe(new RecipePowerShapedEmiRecipe((ShapedRecipe)recipe, powerType, multiplePowerType));
        } else if (recipe instanceof ShapelessRecipe) {
            registry.addRecipe(new RecipePowerShapelessEmiRecipe((ShapelessRecipe)recipe, powerType, multiplePowerType));
        }
    }
}
