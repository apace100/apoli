package io.github.apace100.apoli.screen;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.DynamicContainerType;
import io.github.apace100.apoli.screen.widget.ScrollingTextWidget;
import io.github.apace100.apoli.util.TextureUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class DynamicContainerScreen extends HandledScreen<DynamicContainerScreenHandler> implements ScreenHandlerProvider<DynamicContainerScreenHandler> {

    private static final int SLOT_SIZE = 18;
    private final Identifier texture;

    private final int totalSize;
    private final int rows;

    private final int playerInventoryTitleOffsetX;

    private final int playerInventorySlotsOffsetX;
    private final int playerInventorySlotsOffsetY;

    private final int fillWidth;
    private final int fillOffsetX;
    private final int fillOffsetY;

    public DynamicContainerScreen(DynamicContainerScreenHandler screenHandler, PlayerInventory playerInventory, Text title) {
        super(screenHandler, playerInventory, title);

        int columns = handler.getColumns();
        this.rows = handler.getRows();
        this.totalSize = columns * rows;
        this.fillWidth = Math.max(columns, 5);
        this.backgroundWidth = Math.max(columns, 9) * SLOT_SIZE + 16;
        this.backgroundHeight = rows * SLOT_SIZE + 121;
        this.playerInventoryTitleY = backgroundHeight - 88;

        int halvedSlotSize = SLOT_SIZE / 2;

        //  Calculate the offsets used for filling the GUI with a repeating texture
        int fillOffsetX = fillWidth < 9 ? ((9 - fillWidth) / 2) * SLOT_SIZE : 0;
        if (fillWidth < 9 && fillWidth % 2 == 0) {
            fillOffsetX += halvedSlotSize;
        }

        //  Calculate the offsets used for the title and slots of the player inventory
        int playerInventoryOffsetX = columns > 9 ? ((columns - 9) / 2) * SLOT_SIZE : 0;
        if (columns > 9 && columns % 2 == 0) {
            playerInventoryOffsetX += halvedSlotSize;
        }

        this.fillOffsetX = fillOffsetX;
        this.fillOffsetY = 18;

        this.playerInventoryTitleOffsetX = playerInventoryOffsetX;
        this.playerInventorySlotsOffsetX = playerInventoryOffsetX;
        this.playerInventorySlotsOffsetY = rows * SLOT_SIZE;

        //  Get the specified texture. If the specified texture does not exist, use the default texture instead
        this.texture = TextureUtil
            .tryLoadingTexture(handler.getSpriteLocation())
            .orElseGet(() -> {

                Identifier defaultTexture = DynamicContainerType.DEFAULT_SPRITE_LOCATION;
                Apoli.LOGGER.warn("Loading texture " + defaultTexture + " for dynamic container screen instead...");

                return defaultTexture;

            });

    }

    @Override
    protected void init() {

        super.init();

        //  Use scrolling text widget for the title of the inventory
        ScrollingTextWidget inventoryTitleWidget = new ScrollingTextWidget(fillOffsetX + x + 7, y + 6, fillWidth * SLOT_SIZE, 9, title, false, textRenderer);

        inventoryTitleWidget.setTextColor(4210752);
        inventoryTitleWidget.setAlignment(handler.getTitleAlignment());

        addDrawableChild(inventoryTitleWidget);

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);

        this.drawMouseoverTooltip(context, mouseX, mouseY);

    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(textRenderer, playerInventoryTitle, playerInventoryTitleX + playerInventoryTitleOffsetX, playerInventoryTitleY, 4210752, false);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {

        //  Fill the top and bottom slice of the GUI
        for (int columnIndex = 0; columnIndex < fillWidth; columnIndex++) {
            context.drawTexture(texture, columnIndex * SLOT_SIZE + fillOffsetX + x + 8, y, 8, 0, 18, 18);
            context.drawTexture(texture, columnIndex * SLOT_SIZE + fillOffsetX + x + 8, rows * SLOT_SIZE + fillOffsetY + y, 8, 35, 18, 8);
        }

        //  Draw the top-left, top-right, bottom-left and bottom-right corners of the GUI
        context.drawTexture(texture, fillOffsetX + x, y, 0, 0, 8, 18);
        context.drawTexture(texture, fillWidth * SLOT_SIZE + fillOffsetX + x + 8, y, 25, 0, 8, 18);
        context.drawTexture(texture, fillOffsetX + x, rows * SLOT_SIZE + fillOffsetY + y, 0, 35, 8, 8);
        context.drawTexture(texture, fillWidth * SLOT_SIZE + fillOffsetX + x + 8, rows * SLOT_SIZE + fillOffsetY + y, 25, 35, 8, 8);

        //  Draw the middle slices of the GUI
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {

            //  Draw the left and right borders of the GUI
            context.drawTexture(texture, fillOffsetX + x, rowIndex * SLOT_SIZE + fillOffsetY + y, 0, 18, 8, 18);
            context.drawTexture(texture, fillWidth * SLOT_SIZE + fillOffsetX + x + 8, rowIndex * SLOT_SIZE + fillOffsetY + y, 25, 18, 8, 18);

            //  Fill the GUI with a repeating texture
            for (int columnIndex = 0; columnIndex < fillWidth; columnIndex++) {
                context.drawTexture(texture, columnIndex * SLOT_SIZE + fillOffsetX + x + 8, rowIndex * SLOT_SIZE + fillOffsetY + y, 8, 18, 18, 18);
            }

        }

        //  Draw the player's inventory
        context.drawTexture(texture, playerInventorySlotsOffsetX + x, playerInventorySlotsOffsetY + y + 27, 0, 47, 176, 100);

    }

    public boolean withinBounds(Slot slot) {
        return slot.getIndex() >= 0 && slot.getIndex() < totalSize;
    }

    public void drawSlot(DrawContext context, Slot slot, int textureU, int textureV, int size) {
        size += 1;
        context.drawTexture(texture, slot.x - 1, slot.y - 1, textureU, textureV, size, size);
    }

}
