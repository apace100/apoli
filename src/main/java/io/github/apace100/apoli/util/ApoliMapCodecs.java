package io.github.apace100.apoli.util;

import com.mojang.serialization.MapCodec;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;

public final class ApoliMapCodecs {

    public static final MapCodec<Recipe<?>> RECIPE = Registries.RECIPE_SERIALIZER.getCodec().dispatchMap(Recipe::getSerializer, RecipeSerializer::codec);

}
