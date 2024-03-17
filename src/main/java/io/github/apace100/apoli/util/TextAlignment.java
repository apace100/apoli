package io.github.apace100.apoli.util;

import java.util.Locale;

public enum TextAlignment {

    LEFT((left, right, textWidth) -> left - 1),
    RIGHT((left, right, textWidth) -> right - textWidth + 1),
    CENTER((left, right, textWidth) -> (left + right - textWidth) / 2);

    final PositionSupplier horizontalSupplier;
    TextAlignment(PositionSupplier horizontalSupplier) {
        this.horizontalSupplier = horizontalSupplier;
    }

    public int horizontal(int left, int right, int textWidth) {
        return horizontalSupplier.apply(left, right, textWidth);
    }

    public static TextAlignment from(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "left" -> LEFT;
            case "right" -> RIGHT;
            default -> CENTER;
        };
    }

    @FunctionalInterface
    public interface PositionSupplier {
        int apply(int input1, int input2, int textWidth);
    }

}