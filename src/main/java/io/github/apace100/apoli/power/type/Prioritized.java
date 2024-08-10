package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

        public boolean hasPowers(int priority) {
            return buckets.containsKey(priority);
        }

        public List<T> getPowers(int priority) {
            return buckets.getOrDefault(priority, new LinkedList<>());
        }

        public void forEach(int priority, Consumer<T> action) {
            this.getPowers(priority).forEach(action);
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
