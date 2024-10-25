package io.github.apace100.apoli.data;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.container.ContainerType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class ApoliContainerTypes {

	public static final ContainerType DOUBLE_CHEST = register("double_chest", ContainerType.preset(9, 6, (inventory, columns, rows) -> (syncId, playerInventory, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, inventory, rows)));
	public static final ContainerType CHEST = register("chest", ContainerType.preset(9, 3, (inventory, columns, rows) -> (syncId, playerInventory, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, inventory, rows)));
	public static final ContainerType HOPPER = register("hopper", ContainerType.preset(5, 1, (inventory, columns, rows) -> (syncId, playerInventory, player) -> new HopperScreenHandler(syncId, playerInventory, inventory)));
	public static final ContainerType DROPPER = register("dropper", ContainerType.preset(3, 3, (inventory, columns, rows) -> (syncId, playerInventory, player) -> new Generic3x3ContainerScreenHandler(syncId, playerInventory, inventory)));
	public static final ContainerType DISPENSER = register("dispenser", ContainerType.preset(3, 3, (inventory, columns, rows) -> (syncId, playerInventory, player) -> new Generic3x3ContainerScreenHandler(syncId, playerInventory, inventory)));

	public static void register() {

	}

	public static ContainerType register(String name, ContainerType containerType) {
		return Registry.register(ApoliRegistries.CONTAINER_TYPE, Apoli.identifier(name), containerType);
	}

}
