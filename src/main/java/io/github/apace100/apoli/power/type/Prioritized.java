package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface Prioritized<T extends PowerType & Prioritized<T>> {

    int getPriority();

    class CallInstance<T extends PowerType & Prioritized<T>> {

        private final Map<Integer, List<T>> buckets = new HashMap<>();

        private int minPriority = Integer.MAX_VALUE;
        private int maxPriority = Integer.MIN_VALUE;

        public <U extends T> void add(LivingEntity entity, Class<U> cls) {
            add(entity, cls, u -> true);
        }

        public <U extends T> void add(Entity entity, Class<U> cls, @NotNull Predicate<U> filter) {
            PowerHolderComponent.getPowerTypes(entity, cls)
                .stream()
                .filter(filter)
                .forEach(this::add);
        }

        public int getMinPriority() {
            return minPriority;
        }

        public int getMaxPriority() {
            return maxPriority;
        }

        public boolean hasPowerTypes(int priority) {
            return buckets.containsKey(priority);
        }

        public List<T> getPowerTypes(int priority) {
            return buckets.getOrDefault(priority, new LinkedList<>());
        }

        public List<T> getAllPowerTypes() {
            return buckets.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(Integer::compareTo))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(LinkedList::new));
        }

        public void forEach(int priority, Consumer<T> action) {
            this.getPowerTypes(priority).forEach(action);
        }

        public void add(T t) {

            int priority = t.getPriority();
            buckets.computeIfAbsent(priority, i -> new LinkedList<>())
                .add(t);

            if (priority < minPriority) {
                this.minPriority = priority;
            }

            if (priority > maxPriority) {
                this.maxPriority = priority;
            }

        }

        public boolean isEmpty() {
            return buckets.isEmpty();
        }

    }

}
