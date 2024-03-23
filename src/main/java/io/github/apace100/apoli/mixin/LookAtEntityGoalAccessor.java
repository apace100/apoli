package io.github.apace100.apoli.mixin;

import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LookAtEntityGoal.class)
public interface LookAtEntityGoalAccessor {
    @Accessor
    int getLookTime();
}
