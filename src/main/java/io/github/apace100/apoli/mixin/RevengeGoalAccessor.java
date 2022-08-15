package io.github.apace100.apoli.mixin;

import net.minecraft.entity.ai.goal.RevengeGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RevengeGoal.class)
public interface RevengeGoalAccessor {
    @Accessor
    void setLastAttackedTime(int value);
}
