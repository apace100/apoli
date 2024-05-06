package io.github.apace100.apoli.mixin;

import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MobEntity.class)
public interface MobEntityAccessor {
    @Accessor
    GoalSelector getGoalSelector();

    @Accessor
    GoalSelector getTargetSelector();

    @Invoker
    float invokeChangeAngle(float from, float to, float max);
}