package io.github.apace100.apoli.power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Prioritized<T extends Power & Prioritized<T>> {

    int getPriority();

    class CallInstance<T extends Power & Prioritized<T>> {

        private final HashMap<Integer, List<T>> buckets = new HashMap<>();
        private int minPriority = Integer.MAX_VALUE;
        private int maxPriority = Integer.MIN_VALUE;

        public <U extends T> void add(LivingEntity entity, Class<U> cls) {
            add(entity, cls, null);
        }

        public <U extends T> void add(Entity entity, Class<U> cls, Predicate<U> filter) {
            Stream<U> stream = PowerHolderComponent.getPowers(entity, cls).stream();
            if(filter != null) {
                stream = stream.filter(filter);
            }
            stream.forEach(this::add);
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
            if(buckets.containsKey(priority)) {
                return buckets.get(priority);
            }
            return new LinkedList<>();
        }

        public void add(T t) {
            int priority = t.getPriority();
            if(buckets.containsKey(priority)) {
                buckets.get(priority).add(t);
            } else {
                List<T> list = new LinkedList<>();
                list.add(t);
                buckets.put(priority, list);
            }
            if(priority < minPriority) {
                minPriority = priority;
            }
            if(priority > maxPriority) {
                maxPriority = priority;
            }
        }
    }
}
