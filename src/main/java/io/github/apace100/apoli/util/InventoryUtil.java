package io.github.apace100.apoli.util;

import com.google.common.collect.Sets;
import io.github.apace100.apoli.access.MutableItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.mixin.ItemSlotArgumentTypeAccessor;
import io.github.apace100.apoli.power.InventoryPower;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.ArgumentWrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class InventoryUtil {

    public enum InventoryType {
        INVENTORY,
        POWER
    }

    public enum ProcessMode {
        STACKS(stack -> 1),
        ITEMS(ItemStack::getCount);

        private final Function<ItemStack, Integer> processor;

        ProcessMode(Function<ItemStack, Integer> processor) {
            this.processor = processor;
        }

        public Function<ItemStack, Integer> getProcessor() {
            return processor;
        }
    }

    public static Set<Integer> getSlots(SerializableData.Instance data) {

        Set<Integer> slots = new HashSet<>();

        data.<ArgumentWrapper<Integer>>ifPresent("slot", iaw -> slots.add(iaw.get()));
        data.<List<ArgumentWrapper<Integer>>>ifPresent("slots", iaws -> slots.addAll(iaws.stream().map(ArgumentWrapper::get).toList()));

        if (slots.isEmpty()) slots.addAll(ItemSlotArgumentTypeAccessor.getSlotMappings().values());

        return slots;

    }

    public static int checkInventory(SerializableData.Instance data, Entity entity, InventoryPower inventoryPower, Function<ItemStack, Integer> processor) {

        Predicate<ItemStack> itemCondition = data.get("item_condition");
        Set<Integer> slots = getSlots(data);
        deduplicateSlots(entity, slots);
        int matches = 0;

        if (inventoryPower == null) {
            for (int slot : slots) {

                StackReference stackReference = entity.getStackReference(slot);
                if (stackReference == StackReference.EMPTY) {
                    continue;
                }

                ItemStack stack = stackReference.get();
                if ((itemCondition == null && !stack.isEmpty()) || (itemCondition == null || itemCondition.test(stack))) {
                    matches += processor.apply(stack);
                }

            }
        }

        else {
            for (int slot : slots) {

                if (slot < 0 || slot >= inventoryPower.size()) {
                    continue;
                }

                ItemStack stack = inventoryPower.getStack(slot);
                if ((itemCondition == null && !stack.isEmpty()) || (itemCondition == null || itemCondition.test(stack))) {
                    matches += processor.apply(stack);
                }

            }
        }

        return matches;

    }

    public static void modifyInventory(SerializableData.Instance data, Entity entity, InventoryPower inventoryPower, Function<ItemStack, Integer> processor, int limit) {

        if(limit <= 0) {
            limit = Integer.MAX_VALUE;
        }

        Set<Integer> slots = getSlots(data);
        deduplicateSlots(entity, slots);

        Consumer<Entity> entityAction = data.get("entity_action");
        Predicate<ItemStack> itemCondition = data.get("item_condition");
        ActionFactory<Pair<World, ItemStack>>.Instance itemAction = data.get("item_action");

        int counter = 0;

        if (inventoryPower == null) {
            for(int slot : slots) {

                StackReference stackReference = entity.getStackReference(slot);
                if (stackReference == StackReference.EMPTY) continue;

                ItemStack itemStack = stackReference.get();
                if (itemStack.isEmpty()) continue;

                if (!(itemCondition == null || itemCondition.test(itemStack))) continue;

                if (entityAction != null) entityAction.accept(entity);

                int amount = processor.apply(itemStack);
                for(int i = 0; i < amount; i++) {
                    itemAction.accept(new Pair<>(entity.getWorld(), itemStack));

                    counter += 1;

                    if(counter >= limit) {
                        break;
                    }
                }

                if(counter >= limit) {
                    break;
                }
            }
        } else {
            slots.removeIf(slot -> slot < 0 || slot >= inventoryPower.size());
            for(int slot : slots) {

                ItemStack itemStack = inventoryPower.getStack(slot);
                if (itemStack.isEmpty()) continue;

                if (!(itemCondition == null || itemCondition.test(itemStack))) continue;

                if (entityAction != null) entityAction.accept(entity);

                int amount = processor.apply(itemStack);
                for(int i = 0; i < amount; i++) {
                    itemAction.accept(new Pair<>(entity.getWorld(), itemStack));

                    counter += 1;

                    if(counter >= limit) {
                        break;
                    }
                }

                if(counter >= limit) {
                    break;
                }
            }
        }

    }



    public static void replaceInventory(SerializableData.Instance data, Entity entity, InventoryPower inventoryPower) {

        Set<Integer> slots = getSlots(data);
        deduplicateSlots(entity, slots);

        Consumer<Entity> entityAction = data.get("entity_action");
        Predicate<ItemStack> itemCondition = data.get("item_condition");
        Consumer<Pair<World, ItemStack>> itemAction = data.get("item_action");

        ItemStack replacementStack = data.get("stack");
        boolean mergeNbt = data.getBoolean("merge_nbt");

        if (inventoryPower == null) slots.forEach(
            slot -> {

                StackReference stackReference = entity.getStackReference(slot);
                if (stackReference == StackReference.EMPTY) return;

                ItemStack itemStack = stackReference.get();
                if (!(itemCondition == null || itemCondition.test(itemStack))) return;

                if (entityAction != null) entityAction.accept(entity);

                ItemStack stackAfterReplacement = replacementStack.copy();
                if (mergeNbt && itemStack.hasNbt()) {
                    itemStack.getOrCreateNbt().copyFrom(stackAfterReplacement.getOrCreateNbt());
                    stackAfterReplacement.setNbt(itemStack.getOrCreateNbt());
                }

                stackReference.set(stackAfterReplacement);
                if (itemAction != null) itemAction.accept(new Pair<>(entity.getWorld(), stackAfterReplacement));

            }
        );

        else {
            slots.removeIf(slot -> slot < 0 || slot >= inventoryPower.size());
            slots.forEach(
                slot -> {

                    ItemStack itemStack = inventoryPower.getStack(slot);
                    if (!(itemCondition == null || itemCondition.test(itemStack))) return;

                    if (entityAction != null) entityAction.accept(entity);

                    ItemStack stackAfterReplacement = replacementStack.copy();
                    if (mergeNbt && itemStack.hasNbt()) {
                        itemStack.getOrCreateNbt().copyFrom(stackAfterReplacement.getOrCreateNbt());
                        stackAfterReplacement.setNbt(itemStack.getOrCreateNbt());
                    }

                    inventoryPower.setStack(slot, stackAfterReplacement);
                    if (itemAction != null) itemAction.accept(new Pair<>(entity.getWorld(), stackAfterReplacement));

                }
            );
        }

    }

    public static void dropInventory(SerializableData.Instance data, Entity entity, InventoryPower inventoryPower) {

        Set<Integer> slots = getSlots(data);
        deduplicateSlots(entity, slots);

        int amount = data.getInt("amount");
        boolean throwRandomly = data.getBoolean("throw_randomly");
        boolean retainOwnership = data.getBoolean("retain_ownership");

        Consumer<Entity> entityAction = data.get("entity_action");
        Predicate<ItemStack> itemCondition = data.get("item_condition");
        Consumer<Pair<World, ItemStack>> itemAction = data.get("item_action");

        if (inventoryPower == null) slots.forEach(
            slot -> {

                StackReference stackReference = entity.getStackReference(slot);
                if (stackReference == StackReference.EMPTY) return;

                ItemStack itemStack = stackReference.get();
                if (itemStack.isEmpty()) return;

                if (!(itemCondition == null || itemCondition.test(itemStack))) return;

                if (entityAction != null) entityAction.accept(entity);
                if (itemAction != null) itemAction.accept(new Pair<>(entity.getWorld(), itemStack));

                if (amount != 0) {

                    int newAmount = amount < 0 ? amount * -1 : amount;

                    ItemStack droppedStack = itemStack.split(newAmount);
                    throwItem(entity, droppedStack, throwRandomly, retainOwnership);

                    stackReference.set(itemStack);

                }

                else {
                    throwItem(entity, itemStack, throwRandomly, retainOwnership);
                    stackReference.set(ItemStack.EMPTY);
                }

            }
        );

        else {
            slots.removeIf(slot -> slot < 0 || slot >= inventoryPower.size());
            slots.forEach(
                slot -> {

                    ItemStack itemStack = inventoryPower.getStack(slot);
                    if (itemStack.isEmpty()) return;

                    if (!(itemCondition == null || itemCondition.test(itemStack))) return;

                    if (entityAction != null) entityAction.accept(entity);
                    if (itemAction != null) itemAction.accept(new Pair<>(entity.getWorld(), itemStack));

                    if (amount != 0) {

                        int newAmount = amount < 0 ? amount * -1 : amount;

                        ItemStack droppedStack = itemStack.split(newAmount);
                        throwItem(entity, droppedStack, throwRandomly, retainOwnership);

                        inventoryPower.setStack(slot, itemStack);

                    }

                    else {
                        throwItem(entity, itemStack, throwRandomly, retainOwnership);
                        inventoryPower.setStack(slot, ItemStack.EMPTY);
                    }

                }
            );
        }

    }

    public static void throwItem(Entity thrower, ItemStack itemStack, boolean throwRandomly, boolean retainOwnership) {

        if (itemStack.isEmpty()) return;
        if (thrower instanceof PlayerEntity playerEntity && playerEntity.getWorld().isClient) playerEntity.swingHand(Hand.MAIN_HAND);

        double yOffset = thrower.getEyeY() - 0.30000001192092896D;
        ItemEntity itemEntity = new ItemEntity(thrower.getWorld(), thrower.getX(), yOffset, thrower.getZ(), itemStack);
        itemEntity.setPickupDelay(40);

        Random random = new Random();

        float f;
        float g;

        if (retainOwnership) itemEntity.setThrower(thrower.getUuid());
        if (throwRandomly) {
            f = random.nextFloat() * 0.5F;
            g = random.nextFloat() * 6.2831855F;
            itemEntity.setVelocity(- MathHelper.sin(g) * f, 0.20000000298023224D, MathHelper.cos(g) * f);
        }
        else {
            f = 0.3F;
            g = MathHelper.sin(thrower.getPitch() * 0.017453292F);
            float h = MathHelper.cos(thrower.getPitch() * 0.017453292F);
            float i = MathHelper.sin(thrower.getYaw() * 0.017453292F);
            float j = MathHelper.cos(thrower.getYaw() * 0.017453292F);
            float k = random.nextFloat() * 6.2831855F;
            float l = 0.02F * random.nextFloat();
            itemEntity.setVelocity(
                (double) (- i * h * f) + Math.cos(k) * (double) l,
                (-g * f + 0.1F + (random.nextFloat() - random.nextFloat()) * 0.1F),
                (double) (j * h * f) + Math.sin(k) * (double) l
            );
        }

        thrower.getWorld().spawnEntity(itemEntity);

    }

    private static final Map<Entity, ItemStack> ENTITY_EMPTY_STACK_MAP = new HashMap<>();

    public static ItemStack getEntityLinkedEmptyStack(Entity entity) {
        if (!ENTITY_EMPTY_STACK_MAP.containsKey(entity)) {
            ENTITY_EMPTY_STACK_MAP.put(entity, new ItemStack((Void) null));
        }
        return ENTITY_EMPTY_STACK_MAP.get(entity);
    }

    public static void forEachStack(Entity entity, Consumer<ItemStack> itemStackConsumer) {
        forEachStack(entity, itemStackConsumer, null);
    }

    public static void forEachStack(Entity entity, Consumer<ItemStack> itemStackConsumer, @Nullable Consumer<ItemStack> emptyStackConsumer) {
        int skip = getDuplicatedSlotIndex(entity);

        for(int slot : ItemSlotArgumentTypeAccessor.getSlotMappings().values()) {
            if(slot == skip) {
                skip = Integer.MIN_VALUE;
                continue;
            }
            StackReference stackReference = entity.getStackReference(slot);
            if (stackReference == StackReference.EMPTY) continue;

            ItemStack itemStack = stackReference.get();
            if (itemStack.isEmpty()) {
                if (emptyStackConsumer == null) continue;
                ItemStack newStack = getEntityLinkedEmptyStack(entity);
                emptyStackConsumer.accept(newStack);
                ((MutableItemStack)itemStack).setFrom(newStack);
                continue;
            }
            itemStackConsumer.accept(itemStack);
        }

        Optional<PowerHolderComponent> optionalPowerHolderComponent = PowerHolderComponent.KEY.maybeGet(entity);
        if(optionalPowerHolderComponent.isPresent()) {
            PowerHolderComponent phc = optionalPowerHolderComponent.get();
            List<InventoryPower> inventoryPowers = phc.getPowers(InventoryPower.class);
            for(InventoryPower inventoryPower : inventoryPowers) {
                for(int index = 0; index < inventoryPower.size(); index++) {
                    ItemStack stack = inventoryPower.getStack(index);
                    if (stack.isEmpty()) {
                        if (emptyStackConsumer == null) continue;
                        ItemStack newStack = getEntityLinkedEmptyStack(entity);
                        emptyStackConsumer.accept(newStack);
                        continue;
                    }
                    itemStackConsumer.accept(stack);
                }
            }
        }
    }

    private static void deduplicateSlots(Entity entity, Set<Integer> slots) {
        int hotbarSlot = getDuplicatedSlotIndex(entity);
        if(hotbarSlot != Integer.MIN_VALUE && slots.contains(hotbarSlot)) {
            Integer mainHandSlot = ItemSlotArgumentTypeAccessor.getSlotMappings().get("weapon.mainhand");
            slots.remove(mainHandSlot);
        }
    }

    /**
     * For players, their selected hotbar slot will overlap with the `weapon.mainhand` slot reference.
     * This method returns the slot id of the selected hotbar slot.
     * Otherwise, if no slot is duplicated because the entity is not a player, returns Integer.MIN_VALUE
     * @param entity The entity
     * @return Slot id of hotbar slot if entity is a player, Integer.MIN_VALUE otherwise
     */
    private static int getDuplicatedSlotIndex(Entity entity) {
        if(entity instanceof PlayerEntity player) {
            int selectedSlot = player.getInventory().selectedSlot;
            return ItemSlotArgumentTypeAccessor.getSlotMappings().get("hotbar." + selectedSlot);
        }
        return Integer.MIN_VALUE;
    }
}
