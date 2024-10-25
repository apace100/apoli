package io.github.apace100.apoli.util;

import net.minecraft.util.StringIdentifiable;

import java.util.Optional;

public enum TextAlignment implements StringIdentifiable {

    NONE("none", (left, right, textWidth) -> null),
    LEFT("left", (left, right, textWidth) -> left - 1),
    RIGHT("right", (left, right, textWidth) -> right - textWidth + 1),
    CENTER("center", (left, right, textWidth) -> (left + right - textWidth) / 2);

    final String name;
    final PositionSupplier horizontalSupplier;

    TextAlignment(String name, PositionSupplier horizontalSupplier) {
        this.name = name;
        this.horizontalSupplier = horizontalSupplier;
    }

    @Override
    public String asString() {
        return name;
    }

    public Optional<Integer> horizontal(int left, int right, int textWidth) {
        return Optional.ofNullable(horizontalSupplier.apply(left, right, textWidth));
    }

    @FunctionalInterface
    public interface PositionSupplier {
        Integer apply(int left, int right, int textWidth);
    }

}
