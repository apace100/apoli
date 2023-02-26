package io.github.apace100.apoli.mixin;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.BrainTaskAddition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.Task;
import org.spongepowered.asm.mixin.*;

import java.util.*;

@Mixin(Brain.class)
@Implements(@Interface(iface = BrainTaskAddition.class, prefix = "apoli$"))
public abstract class BrainMixin<E extends LivingEntity> {

    @Shadow @Final private Map<Activity, Set<MemoryModuleType<?>>> forgettingActivityMemories;

    @Shadow @Final private Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryModuleState>>> requiredActivityMemories;

    @Shadow @Final private Map<MemoryModuleType<?>, Optional<? extends Memory<?>>> memories;

    @Shadow @Final private Set<Activity> possibleActivities;

    @Shadow @Final private Map<Integer, Map<Activity, Set<Task<? super E>>>> tasks;

    @Shadow abstract ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> indexTaskList(int begin, ImmutableList<? extends Task<? super E>> tasks);

    @Shadow public abstract void setTaskList(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> indexedTasks, Set<Pair<MemoryModuleType<?>, MemoryModuleState>> requiredMemories, Set<MemoryModuleType<?>> forgettingMemories);

    public void apoli$addToTaskList(Activity activity, int begin, ImmutableList<? extends Task<? super E>> tasks, ImmutableList<MemoryModuleType<?>> memoryTypes) {
        this.possibleActivities.add(activity);
        memoryTypes.forEach(memory -> this.memories.put(memory, Optional.empty()));

        Set<Pair<MemoryModuleType<?>, MemoryModuleState>> set = this.requiredActivityMemories.get(activity) == null ? new HashSet<>() : new HashSet<>(this.requiredActivityMemories.get(activity));
        set.addAll((Collection<? extends Pair<MemoryModuleType<?>, MemoryModuleState>>) memoryTypes.stream().map(memoryModuleType -> Pair.of(memoryModuleType, MemoryModuleState.REGISTERED)).toList());

        Set<MemoryModuleType<?>> forgetSet = this.forgettingActivityMemories.get(activity) == null ? new HashSet<>() : new HashSet<>(this.forgettingActivityMemories.get(activity));
        forgetSet.addAll(memoryTypes);

        List<Pair<Integer, ? extends Task<? super E>>> indexedTasks = new ArrayList<>();
        this.tasks.forEach((key, value) -> {
            if (!value.containsKey(activity)) return;
            value.get(activity).forEach(task -> indexedTasks.add(Pair.of(key, task)));
        });
        indexedTasks.addAll(this.indexTaskList(begin, tasks));

        List<Task<? super E>> orderedTasks = new ArrayList<>();
        indexedTasks.stream().sorted((o1, o2) -> Comparators.min(o1.getFirst(), o2.getFirst())).forEachOrdered(integerMapEntry -> orderedTasks.add(integerMapEntry.getSecond()));
        int min = indexedTasks.stream().max((o1, o2) -> Comparators.min(o1.getFirst(), o2.getFirst())).map(Pair::getFirst).orElse(0);

        this.setTaskList(activity, indexTaskList(min, ImmutableList.copyOf(orderedTasks)), set, forgetSet);
    }
}
