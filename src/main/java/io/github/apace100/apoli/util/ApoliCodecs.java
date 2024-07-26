package io.github.apace100.apoli.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.recipe.PowerCraftingRecipe;
import io.github.apace100.apoli.util.codec.SetCodec;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;

import java.util.EnumSet;
import java.util.function.Function;

public final class ApoliCodecs {

    public static final Codec<EnumSet<AttributeModifierSlot>> ATTRIBUTE_MODIFIER_SLOT_SET = Codec.withAlternative(
        new SetCodec<>(AttributeModifierSlot.CODEC, 1, AttributeModifierSlot.values().length).xmap(EnumSet::copyOf, Function.identity()),
        AttributeModifierSlot.CODEC,
        EnumSet::of
    );

    public static final Codec<CraftingRecipe> CRAFTING_RECIPE = Recipe.CODEC.comapFlatMap(
        recipe -> recipe instanceof CraftingRecipe craftingRecipe
            ? DataResult.success(craftingRecipe)
            : DataResult.error(() -> "Recipe is not a crafting recipe."),
        Function.identity()
    );

    public static final Codec<CraftingRecipe> DISALLOWING_POWER_CRAFTING_RECIPE = CRAFTING_RECIPE.comapFlatMap(
        craftingRecipe -> !(craftingRecipe instanceof PowerCraftingRecipe)
            ? DataResult.success(craftingRecipe)
            : DataResult.error(() -> "Power crafting recipes cannot be nested in each other!"),
        Function.identity()
    );

}
