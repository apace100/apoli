package io.github.apace100.apoli.screen.toast;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.CustomToastData;
import io.github.apace100.apoli.util.TextureUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CustomToast implements PositionAwareToast {

    private static final int TITLE_BASE_COLOR = 16776960;
    private static final int DESCRIPTION_BASE_COLOR = 16777215;

    private final List<OrderedText> title;
    private final List<OrderedText> description;
    private final ItemStack iconStack;
    private final Identifier texture;

    private final int duration;

    private final int titleHeight;
    private final int descriptionHeight;

    private final int alphaShiftEnd;
    private final int heightShift;

    private int height;

    public CustomToast(CustomToastData toastData) {
        this(toastData.title(), toastData.description(), toastData.texture(), toastData.iconStack(), toastData.duration());
    }

    public CustomToast(Text title, Text description, Identifier texture, ItemStack iconStack, int duration) {

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int maxWidth = this.getWidth() - 33;

        this.title = textRenderer.wrapLines(title, maxWidth);
        this.description = textRenderer.wrapLines(description, maxWidth);
        this.iconStack = iconStack;
        this.duration = duration;

        this.titleHeight = 24 + (Math.max(this.title.size(), 1) * 8);
        this.descriptionHeight = 24 + (Math.max(this.description.size(), 1) * 8);

        this.height = titleHeight;
        this.texture = TextureUtil.tryLoadingTexture(texture)
            .result()
            .orElseGet(() -> TextureUtil
                .tryLoadingSprite(texture, TextureUtil.GUI_ATLAS_TEXTURE)
                .resultOrPartial(err -> Apoli.LOGGER.warn("Couldn't load texture \"{}\" as is, or as a sprite! Using default texture \"{}\" instead...", texture, CustomToastData.DEFAULT_TEXTURE))
                .orElse(CustomToastData.DEFAULT_TEXTURE));

        this.alphaShiftEnd = duration / 3;
        this.heightShift = duration / 2;

    }

    @Override
    public Visibility draw(int x, int y, DrawContext context, ToastManager manager, long startTime) {

        TextRenderer textRenderer = manager.getClient().textRenderer;
        int alphaShift = MathHelper.floor(MathHelper.clamp((float) Math.abs(alphaShiftEnd - startTime) / 300, 0.0, 1.0) * 255.0f) << 24 | 67108864;

        int toastTextX = 30;
        int toastTextYCenter = this.getHeight() / 2;

        int titleY = toastTextYCenter - title.size() * 9 / 2;
        int descriptionY = toastTextYCenter - description.size() * 9 / 2;

        int titleYOffset = Math.max(7, titleY);
        int descriptionYOffset = Math.max(7, descriptionY);

        //  Draw the texture and icon of the toast
        context.drawGuiTexture(texture, 0, 0, this.getWidth(), this.getHeight());
        context.drawItemWithoutEntity(iconStack, 8, toastTextYCenter - 8);

        //  If the title and the description only has 1 line, display as is
        if (title.size() == 1 && description.size() == 1) {
            context.drawText(textRenderer, title.get(0), toastTextX, 7, TITLE_BASE_COLOR | 0xFF000000, false);
            context.drawText(textRenderer, description.get(0), toastTextX, 18, -1, false);
        }

        //  If the toast has only been displayed for a certain amount of time,
        //  display and fit the title texts onto the toast and shift its alpha channel (for the fade effect)
        else if (startTime < alphaShiftEnd) {

            context.enableScissor(x + 4, y + 4, x + this.getWidth() - 4, y + this.getHeight() - 4);

            for (var titleLine : title) {
                context.drawText(textRenderer, titleLine, toastTextX, titleYOffset, TITLE_BASE_COLOR | alphaShift, false);
                titleYOffset += 9;
            }

            context.disableScissor();


        }

        //  Otherwise, display and fit the description texts onto the toast
        else {

            context.enableScissor(x + 4, y + 4, x + this.getWidth() - 4, y + this.getHeight() - 4);

            for (var descriptionLine : description) {
                context.drawText(textRenderer, descriptionLine, toastTextX, descriptionYOffset, DESCRIPTION_BASE_COLOR | alphaShift, false);
                descriptionYOffset += 9;
            }

            context.disableScissor();

        }

        this.height = MathHelper.lerp(MathHelper.clamp((float) startTime / (float) heightShift, 0F, 1F), titleHeight, descriptionHeight);
        return startTime >= duration * manager.getNotificationDisplayTimeMultiplier()
            ? Visibility.HIDE
            : Visibility.SHOW;

    }

    @Override
    public int getHeight() {
        return height;
    }

}
