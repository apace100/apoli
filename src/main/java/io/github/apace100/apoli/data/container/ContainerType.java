package io.github.apace100.apoli.data.container;

import com.google.common.base.Preconditions;
import io.github.apace100.apoli.util.TextAlignment;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandlerFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ContainerType(TextAlignment titleAlignment, int columns, int rows, Optional<Factory> factory) {

	public ContainerType {
		Preconditions.checkArgument(columns < 1, "Container type must have at least 1 column!");
		Preconditions.checkArgument(rows < 1, "Container type must have at least 1 row!");
	}

	public ScreenHandlerFactory create(Inventory inventory) {
		return factory()
			.map(factory -> factory.create(inventory, columns(), rows()))
			.orElseThrow(() -> new IllegalStateException("Dynamic screen handler for dynamic container types aren't implemented yet!"));
	}

	public int size() {
		return columns() * rows();
	}

	public static ContainerType preset(int columns, int rows, @NotNull Factory factory) {
		return new ContainerType(TextAlignment.NONE, columns, rows, Optional.of(factory));
	}

	@FunctionalInterface
	public interface Factory {
		ScreenHandlerFactory create(Inventory inventory, int columns, int rows);
	}

}
