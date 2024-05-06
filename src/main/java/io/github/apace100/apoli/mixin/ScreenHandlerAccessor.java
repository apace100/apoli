package io.github.apace100.apoli.mixin;

import net.minecraft.inventory.StackReference;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ScreenHandler.class)
public interface ScreenHandlerAccessor {

    @Invoker
    StackReference callGetCursorStackReference();

}
