package io.github.apace100.apoli.mixin;

import net.minecraft.advancement.Advancement;
import net.minecraft.server.command.AdvancementCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(AdvancementCommand.class)
public interface AdvancementCommandAccessor {

    @Invoker
    static List<Advancement> callSelect(Advancement advancement, AdvancementCommand.Selection selection) {
        throw new AssertionError();
    }

}
