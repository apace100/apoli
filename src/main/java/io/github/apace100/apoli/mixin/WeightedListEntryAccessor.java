package io.github.apace100.apoli.mixin;

import net.minecraft.util.collection.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WeightedList.Entry.class)
public interface WeightedListEntryAccessor {

    @Accessor
    int getWeight();
}
