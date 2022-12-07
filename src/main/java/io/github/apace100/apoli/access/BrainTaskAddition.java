package io.github.apace100.apoli.access;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;

public interface BrainTaskAddition<E extends LivingEntity> {
    void addToTaskList(Activity activity, int begin, ImmutableList<? extends Task<? super E>> tasks, ImmutableList<Pair<MemoryModuleType<?>, MemoryModuleState>> memoryTypes, LivingEntity entity);
}
