package io.github.apace100.apoli.data;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.InventoryPower;
import io.github.apace100.apoli.screen.DynamicContainerScreenHandler;
import io.github.apace100.apoli.util.TextAlignment;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

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

        register(DynamicContainerType.of("generic_9x1", 9, 1, (inventory, columns, rows) ->
            (syncId, playerInventory, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X1, syncId, playerInventory, inventory, rows)
        ));

        register(DynamicContainerType.of("generic_9x2", 9, 2, (inventory, columns, rows) ->
            (syncId, playerInventory, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X2, syncId, playerInventory, inventory, rows)
        ));

        register(DynamicContainerType.of("generic_9x3", 9, 3, (inventory, columns, rows) ->
            (syncId, playerInventory, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, inventory, rows)
        ));

        register(DynamicContainerType.of("generic_9x4", 9, 4, (inventory, columns, rows) ->
            (syncId, playerInventory, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X4, syncId, playerInventory, inventory, rows)
        ));

        register(DynamicContainerType.of("generic_9x5", 9, 5, (inventory, columns, rows) ->
            (syncId, playerInventory, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X5, syncId, playerInventory, inventory, rows)
        ));

        register(DynamicContainerType.of("generic_9x6", 9, 6, (inventory, columns, rows) ->
            (syncId, playerInventory, player) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, inventory, rows)
        ));

    }

    public static final Identifier DEFAULT_SPRITE_LOCATION = Apoli.identifier("textures/gui/container/dynamic.png");

    private final TextAlignment titleAlignment;
    private final Identifier spriteLocation;
    private final Factory handlerFactory;
    private final String name;

    private final int columns;
    private final int rows;

    private DynamicContainerType(String name, int columns, int rows, Factory handlerFactory, Identifier spriteLocation, TextAlignment titleAlignment) {
        this.titleAlignment = titleAlignment;
        this.spriteLocation = spriteLocation;
        this.handlerFactory = handlerFactory;
        this.columns = Math.max(columns, 1);
        this.rows = Math.max(rows, 1);
        this.name = name;
    }

    public static DynamicContainerType of(String name, int columns, int rows, Factory factory) {
        return new DynamicContainerType(name, columns, rows, factory, DEFAULT_SPRITE_LOCATION, TextAlignment.CENTER);
    }

    public static DynamicContainerType of(String name, int columns, int rows, Identifier spriteLocation, TextAlignment titleAlignment) {
        return new DynamicContainerType(name, columns, rows, null, spriteLocation, titleAlignment);
    }

    public ScreenHandlerFactory create(Inventory inventory) {

        if (handlerFactory != null) {
            return handlerFactory.create(inventory, columns, rows);
        }

        return (syncId, playerInventory, player) -> new DynamicContainerScreenHandler(this, syncId, playerInventory, inventory);

    }

    public static DynamicContainerType fromNbt(NbtCompound nbt) {

        TextAlignment titleAlignment = TextAlignment.from(nbt.getString("TitleAlignment"));
        Identifier spriteLocation = Identifier.tryParse(nbt.getString("SpriteLocation"));
        String name = nbt.getString("Name");

        int columns = nbt.getInt("Columns");
        int rows = nbt.getInt("Rows");

        return new DynamicContainerType(name, columns, rows, null, spriteLocation, titleAlignment);

    }

    public NbtCompound toNbt() {

        NbtCompound containerTypeNbt = new NbtCompound();

        containerTypeNbt.putString("Name", name);
        containerTypeNbt.putString("TitleAlignment", titleAlignment.toString());
        containerTypeNbt.putString("SpriteLocation", spriteLocation.toString());
        containerTypeNbt.putInt("Columns", columns);
        containerTypeNbt.putInt("Rows", rows);

        return containerTypeNbt;

    }

    public TextAlignment getTitleAlignment() {
        return titleAlignment;
    }

    public Identifier getSpriteLocation() {
        return spriteLocation;
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
