package io.github.apace100.apoli.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.PowerCraftingObject;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.type.RecipePowerType;
import io.github.apace100.apoli.util.ApoliCodecs;
import io.github.apace100.calio.Calio;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.DataPackContents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public record PowerCraftingRecipe(Identifier powerId, CraftingRecipe delegate) implements CraftingRecipe {

    @Override
    public CraftingRecipeCategory getCategory() {
        return delegate().getCategory();
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {

        if (!(input instanceof PowerCraftingObject pco)) {
            return false;
        }

        RecipePowerType recipePowerType = PowerHolderComponent.KEY.maybeGet(pco.apoli$getPlayer())
            .flatMap(component -> PowerManager.getOptional(powerId).map(component::getPowerType))
            .filter(RecipePowerType.class::isInstance)
            .map(RecipePowerType.class::cast)
            .orElse(null);

        return recipePowerType != null && world.getRecipeManager().get(recipePowerType.getRecipeId())
            .filter(entry -> Objects.equals(this, entry.value()))
            .map(entry -> delegate().matches(input, world))
            .orElse(false);

    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        return delegate().craft(input, lookup);
    }

    @Override
    public boolean fits(int width, int height) {
        return delegate().fits(width, height);
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return delegate().getResult(registriesLookup);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ApoliRecipeSerializers.POWER_CRAFTING;
    }

    @Override
    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput input) {
        return delegate().getRemainder(input);
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return delegate().getIngredients();
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return delegate().isIgnoredInRecipeBook();
    }

    @Override
    public boolean showNotification() {
        return delegate().showNotification();
    }

    @Override
    public String getGroup() {
        return delegate().getGroup();
    }

    private void send(RegistryByteBuf buf) {
        buf.writeIdentifier(powerId());
        Recipe.PACKET_CODEC.encode(buf, delegate());
    }

    private static PowerCraftingRecipe receive(RegistryByteBuf buf) {


        Identifier powerId = buf.readIdentifier();
        Recipe<?> recipe = Recipe.PACKET_CODEC.decode(buf);

        if (recipe instanceof CraftingRecipe craftingRecipe) {
            return new PowerCraftingRecipe(powerId, craftingRecipe);
        }

        else {
            throw new IllegalStateException("Recipe is not a crafting recipe.");
        }

    }

    public static void validatePowerRecipesPostReload() {

        DataPackContents dataPackContents = Calio.DATA_PACK_CONTENTS.get(Unit.INSTANCE);
        if (dataPackContents == null) {
            return;
        }

        RecipeManager recipeManager = dataPackContents.getRecipeManager();
        List<RecipeEntry<?>> recipes = new LinkedList<>(dataPackContents.getRecipeManager().values());

        List<RecipeEntry<PowerCraftingRecipe>> erroredPowerRecipes = recipes
            .stream()
            .filter(recipe -> recipe.value() instanceof PowerCraftingRecipe)
            .map(recipe -> new RecipeEntry<>(recipe.id(), (PowerCraftingRecipe) recipe.value()))
            .filter(recipe -> !PowerManager.contains(recipe.value().powerId()))
            .peek(recipe -> Apoli.LOGGER.error("Removed power recipe \"{}\" which references invalid power \"{}\"!", recipe.id(), recipe.value().powerId()))
            .toList();

        recipes.removeAll(erroredPowerRecipes);
        recipeManager.setRecipes(recipes);

    }

    public static class Serializer implements RecipeSerializer<PowerCraftingRecipe> {

        public static final MapCodec<PowerCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("power").forGetter(PowerCraftingRecipe::powerId),
            ApoliCodecs.DISALLOWING_POWER_CRAFTING_RECIPE.fieldOf("recipe").forGetter(PowerCraftingRecipe::delegate)
        ).apply(instance, PowerCraftingRecipe::new));

        public static final PacketCodec<RegistryByteBuf, PowerCraftingRecipe> PACKET_CODEC = PacketCodec.of(
            PowerCraftingRecipe::send,
            PowerCraftingRecipe::receive
        );

        @Override
        public MapCodec<PowerCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, PowerCraftingRecipe> packetCodec() {
            return PACKET_CODEC;
        }

    }

}
