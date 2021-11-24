package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ItemConditionsClient {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Apoli.identifier("smeltable"), new SerializableData(),
            (data, stack) -> {
                World world = MinecraftClient.getInstance().world;
                if(world == null) {
                    return false;
                }
                Optional<SmeltingRecipe> optional = world.getRecipeManager()
                    .getFirstMatch(
                        RecipeType.SMELTING,
                        new SimpleInventory(stack),
                        world
                    );
                return optional.isPresent();
            }));
    }

    private static void register(ConditionFactory<ItemStack> conditionFactory) {
        Registry.register(ApoliRegistries.ITEM_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
