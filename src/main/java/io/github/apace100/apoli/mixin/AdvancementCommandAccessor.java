package io.github.apace100.apoli.mixin;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.server.command.AdvancementCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(AdvancementCommand.class)
public interface AdvancementCommandAccessor {

    @Invoker
    static void callAddChildrenRecursivelyToList(PlacedAdvancement parent, List<AdvancementEntry> children) {
        throw new AssertionError();
    }

}
