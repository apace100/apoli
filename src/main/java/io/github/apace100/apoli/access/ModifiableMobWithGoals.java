package io.github.apace100.apoli.access;

import io.github.apace100.apoli.behavior.MobBehavior;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.Pair;

import java.util.List;

public interface ModifiableMobWithGoals {
    List<Pair<MobBehavior, Goal>> getModifiedTargetSelectorGoals();
    List<Pair<MobBehavior, Goal>> getModifiedGoalSelectorGoals();
}
