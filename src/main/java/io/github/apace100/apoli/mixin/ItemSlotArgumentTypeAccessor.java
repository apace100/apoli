package io.github.apace100.apoli.mixin;

import net.minecraft.command.argument.ItemSlotArgumentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ItemSlotArgumentType.class)
public interface ItemSlotArgumentTypeAccessor {

    @Accessor("SLOT_NAMES_TO_SLOT_COMMAND_ID")
    static Map<String, Integer> getSlotMappings() {
        throw new AssertionError();
    }

}
