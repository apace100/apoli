package io.github.apace100.apoli.mixin;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.access.BrainTaskAddition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.Task;
import org.spongepowered.asm.mixin.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(Brain.class)
@Implements(@Interface(iface = BrainTaskAddition.class, prefix = "apoli$"))
public abstract class BrainMixin<E extends LivingEntity> {
    @Shadow @Final private Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryModuleState>>> requiredActivityMemories;

    @Shadow @Final private Map<Activity, Set<MemoryModuleType<?>>> forgettingActivityMemories;

    @Shadow public abstract void setTaskList(Activity activity2, ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> indexedTasks, Set<Pair<MemoryModuleType<?>, MemoryModuleState>> requiredMemories, Set<MemoryModuleType<?>> forgettingMemories);

    @Shadow abstract ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> indexTaskList(int begin, ImmutableList<? extends Task<? super E>> tasks);

    @Shadow @Final private Map<MemoryModuleType<?>, Optional<? extends Memory<?>>> memories;

    @Shadow @Final private Set<Activity> possibleActivities;

    public void apoli$addToTaskList(Activity activity, int begin, ImmutableList<? extends Task<? super E>> tasks, ImmutableList<Pair<MemoryModuleType<?>, MemoryModuleState>> memoryTypes, LivingEntity entity) {
        this.possibleActivities.add(activity);
        memoryTypes.forEach(memory -> this.memories.put(memory.getFirst(), Optional.empty()));
        Set<Pair<MemoryModuleType<?>, MemoryModuleState>> set = this.requiredActivityMemories.get(activity) == null ? new HashSet<>() : new HashSet<>(this.requiredActivityMemories.get(activity));
        set.addAll(memoryTypes);
        Set<MemoryModuleType<?>> forgetSet = this.forgettingActivityMemories.get(activity) == null ? new HashSet<>() : new HashSet<>(this.forgettingActivityMemories.get(activity));
        for (Pair<MemoryModuleType<?>, MemoryModuleState> pair : memoryTypes) {
            forgetSet.add(pair.getFirst());
        }
        this.setTaskList(activity, this.indexTaskList(begin, tasks), set, forgetSet);
    }
}
