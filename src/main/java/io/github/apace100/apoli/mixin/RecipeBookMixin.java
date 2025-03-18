package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerCraftingObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.recipe.book.RecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.ref.WeakReference;
import java.util.Objects;

@Mixin(RecipeBook.class)
public abstract class RecipeBookMixin implements PowerCraftingObject {

    @Unique
    private WeakReference<PlayerEntity> apoli$player;

    @Override
    public PlayerEntity apoli$getPlayer() {
        return Objects.requireNonNull(apoli$player.get(), "Player was cleared; recipe book: " + this);
    }

    @Override
    public void apoli$setPlayer(PlayerEntity player) {
        this.apoli$player = new WeakReference<>(player);
    }

}
