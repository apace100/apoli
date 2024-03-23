package io.github.apace100.apoli.mixin;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(Brain.class)
public interface BrainAccessor {
    @Accessor
    Set<Activity> getCoreActivities();

    @Accessor
    Set<Activity> getPossibleActivities();
}
