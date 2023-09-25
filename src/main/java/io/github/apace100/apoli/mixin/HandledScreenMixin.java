package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.screen.DynamicContainerScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("rawtypes")
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void apoli$drawSlotTexture(DrawContext context, Slot slot, CallbackInfo ci) {
        if (((HandledScreen) (Object) this instanceof DynamicContainerScreen dynamicContainerScreen) && dynamicContainerScreen.withinBounds(slot)) {
            dynamicContainerScreen.drawSlot(context, slot, 35, 17, 18);
        }
    }

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", shift = At.Shift.AFTER))
    private void apoli$drawSlotOverlayTexture(DrawContext context, Slot slot, CallbackInfo ci, @Local(ordinal = 1) ItemStack cursorStack) {

        if (!((HandledScreen) (Object) this instanceof DynamicContainerScreen dynamicContainerScreen) || !dynamicContainerScreen.withinBounds(slot)) {
            return;
        }

        //  Draw an overlay texture on the slot if the cursor item stack
        //  cannot be inserted into the said slot
        if (cursorStack.isEmpty() || slot.canInsert(cursorStack)) {
            return;
        }

        RenderSystem.disableDepthTest();
        dynamicContainerScreen.drawSlot(context, slot, 56, 17, 18);
        RenderSystem.enableDepthTest();

    }

}
