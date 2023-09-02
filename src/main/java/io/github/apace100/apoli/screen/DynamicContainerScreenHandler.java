package io.github.apace100.apoli.screen;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.DynamicContainerType;
import io.github.apace100.apoli.power.InventoryPower;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;

public class DynamicContainerScreenHandler extends SyncedGuiDescription {

    public DynamicContainerScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(null, syncId, playerInventory, null);
    }

    public DynamicContainerScreenHandler(DynamicContainerType containerType, int syncId, PlayerInventory playerInventory, Inventory otherInventory) {
        super(ApoliScreenHandlerType.DYNAMIC_CONTAINER, syncId, playerInventory, otherInventory, null);

        PlayerEntity playerEntity = playerInventory.player;
        boolean clientSide = playerEntity.getWorld().isClient;

        if (clientSide || (containerType == null || otherInventory == null)) {

            InventoryPower inventoryPower = PowerHolderComponent.getPowers(playerEntity, InventoryPower.class)
                .stream()
                .filter(InventoryPower::isOpened)
                .findFirst()
                .orElse(null);

            containerType = inventoryPower != null ? inventoryPower.getContainerType() : InventoryPower.ContainerType.DROPPER.getDynamicType();
            otherInventory = inventoryPower != null ? inventoryPower : new SimpleInventory(containerType.getSize());

        }

        final Inventory finalOtherInventory = otherInventory;
        finalOtherInventory.onOpen(playerEntity);

        if (!clientSide) {
            PowerHolderComponent.sync(playerEntity);
        }

        int columns = containerType.getColumns();
        int rows = containerType.getRows();

        int slotSize = 18;

        //  TODO: Use a scroll panel for the slot grid panel
        WPlainPanel root = new WPlainPanel();
        WGridPanel slotGrid = new WGridPanel();

        setRootPanel(root);
        root.setInsets(Insets.ROOT_PANEL);

        WItemSlot slot;
        int invIndex = 0;

        //  Prepare the slot grid
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            for (int columnIndex = 0; columnIndex < columns; columnIndex++) {

                int finalInvIndex = invIndex;

                slot = WItemSlot.of(finalOtherInventory, invIndex);
                slot.setFilter(stack -> finalOtherInventory.isValid(finalInvIndex, stack));

                slotGrid.add(slot, columnIndex, rowIndex);
                invIndex++;

            }
        }

        //  Center player's inventory relative to the slot grid if the slot grid is bigger
        int playerInvY = rows * slotSize + 15;
        int playerInvX = columns > 9 ? ((columns - 9) / 2) * slotSize : 0;
        if (columns > 9 && columns % 2 == 0) {
            playerInvX += slotSize / 2;
        }

        //  Center the slot grid relative to the player's inventory otherwise
        int slotGridY = 12;
        int slotGridX = columns < 9 ? ((9 - columns) / 2) * slotSize : 0;
        if (columns < 9 && columns % 2 == 0) {
            slotGridX += slotSize / 2;
        }

        root.add(createPlayerInventoryPanel(false), playerInvX, playerInvY);
        root.add(slotGrid, slotGridX, slotGridY);

        root.validate(this);

    }

}
