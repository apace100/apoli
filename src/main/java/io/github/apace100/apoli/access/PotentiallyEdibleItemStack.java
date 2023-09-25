package io.github.apace100.apoli.access;

import net.minecraft.item.FoodComponent;

import java.util.Optional;

public interface PotentiallyEdibleItemStack {
    Optional<FoodComponent> apoli$getFoodComponent();
}
