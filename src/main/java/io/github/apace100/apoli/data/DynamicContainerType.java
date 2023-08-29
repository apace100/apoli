package io.github.apace100.apoli.data;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.InventoryPower;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DynamicContainerType {

    private static final Map<String, DynamicContainerType> MAP = new HashMap<>();

    /**
     *      <p>Register the specified {@link DynamicContainerType} to the registry. <b>Registering a
     *      {@link DynamicContainerType} with the same name as another is not allowed.</b></p>
     *
     *      @param dynamicContainerType     The {@link DynamicContainerType} to register.
     */
    public static void register(DynamicContainerType dynamicContainerType) {

        String name = dynamicContainerType.getName();
        if (MAP.containsKey(name)) {
            Apoli.LOGGER.error("Cannot register container type \"{}\", as it's already registered!", name);
            return;
        }

        MAP.put(name, dynamicContainerType);

    }

    /**
     *      <p>Get the {@link DynamicContainerType} that associates with the specified {@linkplain String string}.</p>
     *
     *      @param name                         the name to get its associating {@link DynamicContainerType}.
     *      @return                             the {@link DynamicContainerType} associated with the specified string.
     *      @throws IllegalArgumentException    if the specified string is not associated with any
     *                                              {@link DynamicContainerType} in the registry.
     */
    public static DynamicContainerType get(String name) {

        if (!MAP.containsKey(name)) {
            String possibleValues = String.join(", ", MAP.keySet());
            throw new IllegalArgumentException("Expected container type to be any of " + possibleValues);
        }

        return MAP.get(name);

    }

    public static void register() {

        for (InventoryPower.ContainerType oldContainerType : InventoryPower.ContainerType.values()) {
            register(oldContainerType.getDynamicType());
        }

        register(new DynamicContainerType("generic_9x1", 9, 1, (inventory, columns, rows) ->
            (syncId, playerInventory, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X1, syncId, playerInventory, inventory, rows)
        ));

        register(new DynamicContainerType("generic_9x2", 9, 2, (inventory, columns, rows) ->
            (syncId, playerInventory, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X2, syncId, playerInventory, inventory, rows)
        ));

        register(new DynamicContainerType("generic_9x3", 9, 3, (inventory, columns, rows) ->
            (syncId, playerInventory, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, inventory, rows)
        ));

        register(new DynamicContainerType("generic_9x4", 9, 4, (inventory, columns, rows) ->
            (syncId, playerInventory, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X4, syncId, playerInventory, inventory, rows)
        ));

        register(new DynamicContainerType("generic_9x5", 9, 5, (inventory, columns, rows) ->
            (syncId, playerInventory, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X5, syncId, playerInventory, inventory, rows)
        ));

        register(new DynamicContainerType("generic_9x6", 9, 6, (inventory, columns, rows) ->
            (syncId, playerInventory, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, inventory, rows)
        ));

    }

    private final Factory handlerFactory;

    private final String name;
    private final int columns;
    private final int rows;

    public DynamicContainerType(String name, int columns, int rows, Factory handlerFactory) {
        this.handlerFactory = handlerFactory;
        this.name = name;
        this.columns = columns;
        this.rows = rows;
    }

    public ScreenHandlerFactory create(Inventory inventory) {
        return handlerFactory.create(inventory, columns, rows);
    }

    public String getName() {
        return name;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getSize() {
        return columns * rows;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, getSize());
    }

    @Override
    public boolean equals(Object o) {
        return this == o
            || (o instanceof DynamicContainerType other && name.equals(other.name) && getSize() == other.getSize());
    }

    public interface Factory {
        ScreenHandlerFactory create(Inventory inventory, int columns, int rows);
    }

}
