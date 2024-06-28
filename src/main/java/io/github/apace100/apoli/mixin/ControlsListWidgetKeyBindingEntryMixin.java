package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.util.KeybindRegistry;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ControlsListWidget.KeyBindingEntry.class)
public class ControlsListWidgetKeyBindingEntryMixin {

    @Mutable
    @Shadow @Final private Text bindingName;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void changeDisplayName(ControlsListWidget controlsListWidget, KeyBinding binding, Text bindingName, CallbackInfo ci) {
        KeybindRegistry.forEach(((identifier, keyBindingData) -> {
            if(keyBindingData.getTranslationKey().equals(binding.getTranslationKey())) {
                this.bindingName = keyBindingData.getName();
            }
        }));

    }
}
