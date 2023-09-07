package io.github.apace100.apoli.screen;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.DynamicContainerType;
import io.github.apace100.apoli.power.InventoryPower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class DynamicContainerScreenHandler extends ScreenHandler {

    private final Inventory inventory;

    private final int totalSize;
    private final int columns;
    private final int rows;

    public DynamicContainerScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(null, syncId, playerInventory, null);
    }

    public DynamicContainerScreenHandler(DynamicContainerType containerType, int syncId, PlayerInventory playerInventory, Inventory otherInventory) {
        super(ApoliScreenHandlerType.DYNAMIC_CONTAINER, syncId);

        PlayerEntity playerEntity = playerInventory.player;
        if (containerType == null || otherInventory == null) {

            InventoryPower inventoryPower = PowerHolderComponent.getPowers(playerEntity, InventoryPower.class)
                .stream()
                .filter(InventoryPower::isOpened)
                .findFirst()
                .orElse(null);

            containerType = inventoryPower != null ? inventoryPower.getContainerType() : InventoryPower.ContainerType.DROPPER.getDynamicType();
            otherInventory = inventoryPower != null ? inventoryPower : new SimpleInventory(containerType.getSize());

        }

        inventory = otherInventory;
        inventory.onOpen(playerEntity);

        totalSize = Math.max(containerType.getSize(), 1);
        columns = Math.max(containerType.getColumns(), 1);
        rows = Math.max(containerType.getRows(), 1);

        int slotSize = 18;
        int invIndex = 0;

        int offsetY;
        int offsetX = columns < 9 ? ((9 - columns) / 2) * slotSize : 0;
        if (columns < 9 && columns % 2 == 0) {
            offsetX += slotSize / 2;
        }

        Slot slot;

        //  Set up the other inventory's slot grid
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            for (int columnIndex = 0; columnIndex < columns; columnIndex++) {
                 slot = createFilteredSlot(invIndex++, columnIndex * slotSize + offsetX + 8, rowIndex * slotSize + 18);
                 this.addSlot(slot);
            }
        }

        offsetY = rows * slotSize;
        offsetX = columns > 9 ? ((columns - 9) / 2) * slotSize : 0;
        if (columns > 9 && columns % 2 == 0) {
            offsetX += slotSize / 2;
        }

        //  Set up the player's hotbar slot grid
        invIndex = 0;
        for (int columnIndex = 0; columnIndex < 9; columnIndex++) {
            slot = new Slot(playerInventory, invIndex++, columnIndex * slotSize + offsetX + 8, offsetY + 30 + slotSize * 4);
            this.addSlot(slot);
        }

        //  Set up the player inventory's slot grid
        for (int rowIndex = 0; rowIndex < 3; rowIndex++) {
            for (int columnIndex = 0; columnIndex < 9; columnIndex++) {
                slot = new Slot(playerInventory, invIndex++, columnIndex * slotSize + offsetX + 8, rowIndex * 18 + offsetY + 44);
                this.addSlot(slot);
            }
        }

    }

    private Slot createFilteredSlot(int index, int x, int y) {
        return inventory instanceof InventoryPower inventoryPower ? inventoryPower.new FilteredSlot(index, x, y)
                                                                  : new Slot(inventory, index, x, y);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {

        Slot chosenSlot = this.slots.get(slot);
        if (!chosenSlot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = chosenSlot.getStack().copy();
        boolean cannotInsert = slot < totalSize ? !this.insertItem(stack, totalSize, this.slots.size(), true)
                                                : !this.insertItem(stack, 0, totalSize, false);

        if (cannotInsert) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            chosenSlot.setStack(ItemStack.EMPTY);
        } else {
            chosenSlot.markDirty();
        }

        return stack;

    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

}
