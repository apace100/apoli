package io.github.apace100.apoli.mixin;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.Memory;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(Brain.class)
public interface BrainAccessor {
    @Accessor
    Map<MemoryModuleType<?>, Optional<? extends Memory<?>>> getMemories();

    @Accessor
    Map<Activity, Set<MemoryModuleType<?>>> getForgettingActivityMemories();
}
