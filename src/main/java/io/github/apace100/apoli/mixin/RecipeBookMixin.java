package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerCraftingBook;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.book.RecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RecipeBook.class)
public abstract class RecipeBookMixin implements PowerCraftingBook {

    @Unique
    private PlayerEntity apoli$player;

    @Override
    public PlayerEntity apoli$getPlayer() {
        return apoli$player;
    }

    @Override
    public void apoli$setPlayer(PlayerEntity player) {
        this.apoli$player = player;
    }

}
