package io.github.apace100.apoli.screen.toast;

import io.github.apace100.apoli.data.CustomToastData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CustomToast implements Toast {

    private static final Identifier TEXTURE = new Identifier("toast/advancement");

    private static final int TITLE_BASE_COLOR = 16776960;
    private static final int DESCRIPTION_BASE_COLOR = 16777215;
    private static final int ALPHA_SHIFT_END = 1500;

    private final Text title;
    private final List<OrderedText> description;
    private final ItemStack iconStack;

    @Nullable private final SoundEvent soundEvent;

    private final int duration;
    private boolean playedSound = false;

    public CustomToast(CustomToastData toastData) {
        this(toastData.title(), toastData.description(), toastData.iconStack(), toastData.soundEvent(), toastData.duration());
    }

    public CustomToast(Text title, Text description, ItemStack iconStack, @Nullable SoundEvent soundEvent, int duration) {
        this.title = title;
        this.description = MinecraftClient.getInstance().textRenderer.wrapLines(description, this.getWidth() - 13);
        this.iconStack = iconStack;
        this.soundEvent = soundEvent;
        this.duration = duration;
    }

    @Override
    public Visibility draw(DrawContext context, ToastManager manager, long startTime) {

        //  Draw the texture for the toast
        TextRenderer textRenderer = manager.getClient().textRenderer;
        context.drawGuiTexture(TEXTURE, 0, 0, this.getWidth(), this.getHeight());

        int alphaShift = MathHelper.floor(MathHelper.clamp((float) Math.abs(ALPHA_SHIFT_END - startTime) / 300, 0.0, 1.0) * 255.0f) << 24 | 67108864;

        //  If the description text is only 1 line, display it as is
        if (description.size() == 1) {
            context.drawText(textRenderer, title, 30, 7, TITLE_BASE_COLOR | 0xFF000000, false);
            context.drawText(textRenderer, description.get(0), 28, 18, -1, false);
        }

        //  Else, if the drawing process is currently within the shifting phase, shift the alpha of the title text
        else if (startTime < ALPHA_SHIFT_END) {
            context.drawText(textRenderer, title, 30, 11, TITLE_BASE_COLOR | alphaShift, false);
        }

        //  Otherwise, fit the description text into the toast window
        //  TODO: Make the toast expand horizontally if the description and title is too long (e.g: more than 1 line)
        else {

            int yOffset = Math.max(7, this.getHeight() / 2 - description.size() * 9 / 2);

            for (OrderedText descriptionLine : description) {
                context.drawText(textRenderer, descriptionLine, 28, yOffset, DESCRIPTION_BASE_COLOR | alphaShift, false);
                yOffset += 9;
            }

        }

        //  If a sound event is specified, play it once
        if (soundEvent != null && !this.playedSound && startTime > 0) {
            this.playedSound = true;
            manager.getClient().getSoundManager().play(PositionedSoundInstance.master(soundEvent, 1.0f, 1.0f));
        }

        //  Draw the item stack used as the icon
        context.drawItemWithoutEntity(iconStack, 8, 8);
        return startTime >= duration * manager.getNotificationDisplayTimeMultiplier() ? Visibility.HIDE : Visibility.SHOW;

    }

}
