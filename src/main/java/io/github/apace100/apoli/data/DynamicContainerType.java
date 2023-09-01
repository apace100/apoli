package io.github.apace100.apoli.data;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.InventoryPower;
import io.github.apace100.apoli.screen.DynamicContainerScreenHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class DynamicContainerType {

    private static final Map<String, DynamicContainerType> MAP = new LinkedHashMap<>();

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

    public static void registerAll() {

        for (InventoryPower.ContainerType oldContainerType : InventoryPower.ContainerType.values()) {
            register(oldContainerType.getDynamicType());
        }

        for (int i = 1; i <= 6; i++) {
            register(DynamicContainerType.of("generic_9x" + i, 9, i));
        }

    }

    private final Factory handlerFactory;
    private final String name;

    private final int columns;
    private final int rows;

    private DynamicContainerType(String name, int columns, int rows, Factory handlerFactory) {
        this.handlerFactory = handlerFactory;
        this.columns = columns;
        this.rows = rows;
        this.name = name;
    }

    public static DynamicContainerType of(String name, int columns, int rows, Factory factory) {
        return new DynamicContainerType(name, columns, rows, factory);
    }

    private DynamicContainerType(String name, int columns, int rows) {
        this(name, columns, rows, null);
    }

    public static DynamicContainerType of(String name, int columns, int rows) {
        return new DynamicContainerType(name, columns, rows);
    }

    public ScreenHandlerFactory create(Inventory inventory) {

        if (handlerFactory != null) {
            return handlerFactory.create(inventory, columns, rows);
        }

        return (syncId, playerInventory, player) -> new DynamicContainerScreenHandler(this, syncId, playerInventory, inventory);

    }

    public static DynamicContainerType fromNbt(NbtCompound nbt) {

        String name = nbt.getString("Name");
        int columns = nbt.getInt("Columns");
        int rows = nbt.getInt("Rows");

        return new DynamicContainerType(name, columns, rows);

    }

    public NbtCompound toNbt() {

        NbtCompound containerTypeNbt = new NbtCompound();

        containerTypeNbt.putString("Name", name);
        containerTypeNbt.putInt("Columns", columns);
        containerTypeNbt.putInt("Rows", rows);

        return containerTypeNbt;

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
