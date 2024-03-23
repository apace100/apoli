package io.github.apace100.apoli.screen.toast;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.CustomToastData;
import io.github.apace100.apoli.util.TextureUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CustomToast implements Toast {

    private static final int TITLE_BASE_COLOR = 16776960;
    private static final int DESCRIPTION_BASE_COLOR = 16777215;

    private final Text title;
    private final List<OrderedText> description;
    private final ItemStack iconStack;
    private final Identifier texture;

    private final int alphaShiftEnd;
    private final int duration;

    public CustomToast(CustomToastData toastData) {
        this(toastData.title(), toastData.description(), toastData.texture(), toastData.iconStack(), toastData.duration());
    }

    public CustomToast(Text title, Text description, Identifier texture, ItemStack iconStack, int duration) {
        this.title = title;
        this.description = MinecraftClient.getInstance().textRenderer.wrapLines(description, this.getWidth() - 33);
        this.iconStack = iconStack;
        this.texture = TextureUtil.tryLoadingSprite(texture, TextureUtil.GUI_ATLAS_TEXTURE)
            .resultOrPartial(err -> {
                Apoli.LOGGER.error(err);
                Apoli.LOGGER.warn("Using default texture (\"{}\") instead...", CustomToastData.DEFAULT_TEXTURE);
            })
            .orElse(CustomToastData.DEFAULT_TEXTURE);
        this.alphaShiftEnd = duration / 3;
        this.duration = duration;
    }

    //  TODO:   Handle long titles, like how long descriptions are handled
    @Override
    public Visibility draw(DrawContext context, ToastManager manager, long startTime) {

        TextRenderer textRenderer = manager.getClient().textRenderer;

        int alphaShift = MathHelper.floor(MathHelper.clamp((float) Math.abs(alphaShiftEnd - startTime) / 300, 0.0, 1.0) * 255.0f) << 24 | 67108864;
        int centeredY = this.getHeight() / 2;

        int x = 30;
        int descriptionY = centeredY - description.size() * 9 / 2;

        //  Draw the texture and icon of the toast
        context.drawGuiTexture(texture, 0, 0, this.getWidth(), this.getHeight());
        context.drawItemWithoutEntity(iconStack, 8, centeredY - 8);

        //  If the description text is only 1 line, display it as is
        if (description.size() == 1) {
            context.drawText(textRenderer, title, x, 7, TITLE_BASE_COLOR | 0xFF000000, false);
            context.drawText(textRenderer, description.get(0), x, 18, -1, false);
        }

        //  Else, if the drawing process is currently within the shifting phase, shift the alpha of the title text
        else if (startTime < alphaShiftEnd) {
            context.drawText(textRenderer, title, x, centeredY - 8, TITLE_BASE_COLOR | alphaShift, false);
        }

        //  Otherwise, fit the description text into the toast window
        else {

            int yOffset = Math.max(7, descriptionY);

            for (OrderedText descriptionLine : description) {
                context.drawText(textRenderer, descriptionLine, x, yOffset, DESCRIPTION_BASE_COLOR | alphaShift, false);
                yOffset += 9;
            }

        }

        return startTime >= duration * manager.getNotificationDisplayTimeMultiplier() ? Visibility.HIDE : Visibility.SHOW;

    }

    @Override
    public int getHeight() {
        return 24 + Math.min(Math.max(this.description.size(), 1), 3) * 8;
    }

}
