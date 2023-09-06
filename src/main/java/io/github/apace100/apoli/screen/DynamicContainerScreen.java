package io.github.apace100.apoli.screen;

import io.github.apace100.apoli.Apoli;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class DynamicContainerScreen extends HandledScreen<DynamicContainerScreenHandler> implements ScreenHandlerProvider<DynamicContainerScreenHandler> {

    private static final Identifier TEXTURE = Apoli.identifier("textures/gui/container/dynamic.png");
    private static final int SLOT_SIZE = 18;

    private final int columns;
    private final int rows;

    private final int playerInventoryTitleOffsetX;
    private final int inventoryTitleOffsetX;

    private final int playerInventorySlotsOffsetX;
    private final int playerInventorySlotsOffsetY;
    private final int inventorySlotsOffsetX;
    private final int inventorySlotsOffsetY;

    private final int fillWidth;
    private final int fillOffsetX;
    private final int fillOffsetY;

    public DynamicContainerScreen(DynamicContainerScreenHandler screenHandler, PlayerInventory playerInventory, Text title) {
        super(screenHandler, playerInventory, title);

        this.columns = Math.max(handler.getColumns(), 1);
        this.rows = Math.max(handler.getRows(), 1);
        this.fillWidth = Math.max(columns, 5);
        this.backgroundWidth = Math.max(columns, 9) * SLOT_SIZE + 16;
        this.backgroundHeight = rows * SLOT_SIZE + 121;
        this.playerInventoryTitleY = backgroundHeight - 88;

        int halvedSlotSize = SLOT_SIZE / 2;
        int columnDiff = (columns > 9 ? columns - 9 : 9 - columns) / 2;

        this.playerInventoryTitleOffsetX = columns > 9 ? columnDiff * SLOT_SIZE : 0;
        this.inventoryTitleOffsetX = columns < 9 ? columnDiff * SLOT_SIZE : 0;

        int playerInventorySlotsOffsetX = columns > 9 ? columnDiff * SLOT_SIZE : 0;
        int inventorySlotsOffsetX = columns < 9 ? columnDiff * SLOT_SIZE : 0;

        int fillOffsetX = fillWidth < 9 ? ((9 - fillWidth) / 2) * SLOT_SIZE : 0;
        if (fillWidth % 2 == 0) {
            fillOffsetX += halvedSlotSize;
        }

        if (columns % 2 == 0) {
            if (columns > 9) {
                playerInventorySlotsOffsetX += halvedSlotSize;
            } else {
                inventorySlotsOffsetX += halvedSlotSize;
            }
        }

        this.playerInventorySlotsOffsetX = playerInventorySlotsOffsetX;
        this.inventorySlotsOffsetX = inventorySlotsOffsetX;
        this.playerInventorySlotsOffsetY = rows * SLOT_SIZE;
        this.inventorySlotsOffsetY = 18;

        this.fillOffsetX = fillOffsetX;
        this.fillOffsetY = 18;

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);

        this.drawMouseoverTooltip(context, mouseX, mouseY);

    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(textRenderer, title, titleX + inventoryTitleOffsetX, titleY, 4210752, false);
        context.drawText(textRenderer, playerInventoryTitle, playerInventoryTitleX + playerInventoryTitleOffsetX, playerInventoryTitleY, 4210752, false);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {

        //  Fill the top and bottom parts of the GUI
        for (int columnIndex = 0; columnIndex < fillWidth; columnIndex++) {
            context.drawTexture(TEXTURE, columnIndex * SLOT_SIZE + fillOffsetX + x + 8, y, 8, 0, 18, 18);
            context.drawTexture(TEXTURE, columnIndex * SLOT_SIZE + fillOffsetX + x + 8, rows * SLOT_SIZE + fillOffsetY + y, 8, 35, 18, 8);
        }

        //  Draw the top-left, top-right, bottom-left and bottom-right corners of the GUI
        context.drawTexture(TEXTURE, fillOffsetX + x, y, 0, 0, 8, 18);
        context.drawTexture(TEXTURE, fillWidth * SLOT_SIZE + fillOffsetX + x + 8, y, 25, 0, 8, 18);
        context.drawTexture(TEXTURE, fillOffsetX + x, rows * SLOT_SIZE + fillOffsetY + y, 0, 35, 8, 8);
        context.drawTexture(TEXTURE, fillWidth * SLOT_SIZE + fillOffsetX + x + 8, rows * SLOT_SIZE + fillOffsetY + y, 25, 35, 8, 8);

        //  Draw the slots of the inventory
        int invIndex = 0;
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {

            //  Draw the left and right borders of the GUI
            context.drawTexture(TEXTURE, fillOffsetX + x, rowIndex * SLOT_SIZE + fillOffsetY + y, 0, 18, 8, 18);
            context.drawTexture(TEXTURE, fillWidth * SLOT_SIZE + fillOffsetX + x + 8, rowIndex * SLOT_SIZE + fillOffsetY + y, 25, 18, 8, 18);

            //  Fill the GUI with a repeating texture
            for (int columnIndex = 0; columnIndex < fillWidth; columnIndex++) {
                context.drawTexture(TEXTURE, columnIndex * SLOT_SIZE + fillOffsetX + x + 8, rowIndex * SLOT_SIZE + fillOffsetY + y, 8, 18, 18, 18);
            }

            //  Draw the slots of the GUI
            for (int columnIndex = 0; columnIndex < columns; columnIndex++) {

                ItemStack cursorStack = handler.getCursorStack();
                Slot slot = handler.getSlot(invIndex++);

                //  If the cursor stack is either empty or cannot be inserted into
                //  the current slot, change the texture of the slot
                int slotU = cursorStack.isEmpty() || slot.canInsert(cursorStack) ? 35 : 56;
                context.drawTexture(TEXTURE, columnIndex * SLOT_SIZE + inventorySlotsOffsetX + x + 7, rowIndex * SLOT_SIZE + inventorySlotsOffsetY + y - 1, slotU, 17, 19, 19);

            }

        }

        //  Draw the player's inventory
        context.drawTexture(TEXTURE, playerInventorySlotsOffsetX + x, playerInventorySlotsOffsetY + y + 27, 0, 47, 176, 100);

    }

}
