package io.github.apace100.apoli.power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ActiveInteractionPower extends InteractionPower {

    private final int priority;

    public ActiveInteractionPower(PowerType<?> type, LivingEntity entity, EnumSet<Hand> hands, ActionResult actionResult, Predicate<ItemStack> itemCondition, Consumer<Pair<World, ItemStack>> heldItemAction, ItemStack itemResult, Consumer<Pair<World, ItemStack>> resultItemAction, int priority) {
        super(type, entity, hands, actionResult, itemCondition, heldItemAction, itemResult, resultItemAction);
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public static class CallInstance<T extends ActiveInteractionPower> {

        private HashMap<Integer, List<T>> buckets = new HashMap<>();
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
