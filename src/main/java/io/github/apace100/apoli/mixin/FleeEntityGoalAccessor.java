package io.github.apace100.apoli.mixin;

import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FleeEntityGoal.class)
public interface FleeEntityGoalAccessor {
    @Accessor @Mutable
    void setWithinRangePredicate(TargetPredicate predicate);
}
