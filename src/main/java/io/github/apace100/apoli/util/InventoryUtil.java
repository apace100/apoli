package io.github.apace100.apoli.util;

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

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class InventoryUtil {

    public enum InventoryType {
        INVENTORY,
        POWER
    }

    public static Set<Integer> getSlots(SerializableData.Instance data) {

        Set<Integer> slots = new HashSet<>();

        data.<ArgumentWrapper<Integer>>ifPresent("slot", iaw -> slots.add(iaw.get()));
        data.<List<ArgumentWrapper<Integer>>>ifPresent("slots", iaws -> slots.addAll(iaws.stream().map(ArgumentWrapper::get).toList()));

        if (slots.isEmpty()) slots.addAll(ItemSlotArgumentTypeAccessor.getSlotMappings().values());

        return slots;

    }

    public static boolean checkInventory(SerializableData.Instance data, Entity entity, InventoryPower inventoryPower) {

        AtomicInteger matches = new AtomicInteger();

        Predicate<ItemStack> itemCondition = data.get("item_condition");
        Set<Integer> slots = getSlots(data);
        Comparison comparison = data.get("comparison");
        int compareTo = data.get("compare_to");

        if (inventoryPower == null) slots.forEach(
            slot -> {

                StackReference stackReference = entity.getStackReference(slot);
                if (stackReference == StackReference.EMPTY) return;

                ItemStack stack = stackReference.get();
                if (itemCondition == null || stack.isEmpty()) return;
                else if (!itemCondition.test(stack)) return;

                matches.incrementAndGet();

            }
        );

        else slots.forEach(
            slot -> {

                if (slot < 0 || slot >= inventoryPower.size()) return;

                ItemStack stack = inventoryPower.getStack(slot);
                if (itemCondition == null || stack.isEmpty()) return;
                else if (!itemCondition.test(stack)) return;

                matches.incrementAndGet();

            }
        );

        return comparison.compare(matches.get(), compareTo);

    }

    public static void modifyInventory(SerializableData.Instance data, Entity entity, InventoryPower inventoryPower) {

        Set<Integer> slots = getSlots(data);

        Consumer<Entity> entityAction = data.get("entity_action");
        Predicate<ItemStack> itemCondition = data.get("item_condition");
        ActionFactory<Pair<World, ItemStack>>.Instance itemAction = data.get("item_action");

        if (inventoryPower == null) slots.forEach(
            slot -> {

                StackReference stackReference = entity.getStackReference(slot);
                if (stackReference == StackReference.EMPTY) return;

                ItemStack itemStack = stackReference.get();
                if (itemStack.isEmpty()) return;

                if (!(itemCondition == null || itemCondition.test(itemStack))) return;

                if (entityAction != null) entityAction.accept(entity);
                itemAction.accept(new Pair<>(entity.world, itemStack));

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
                    itemAction.accept(new Pair<>(entity.world, itemStack));

                }
            );
        }

    }

    public static void replaceInventory(SerializableData.Instance data, Entity entity, InventoryPower inventoryPower) {

        Set<Integer> slots = getSlots(data);

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
                if (itemAction != null) itemAction.accept(new Pair<>(entity.world, stackAfterReplacement));

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
                    if (itemAction != null) itemAction.accept(new Pair<>(entity.world, stackAfterReplacement));

                }
            );
        }

    }

    public static void dropInventory(SerializableData.Instance data, Entity entity, InventoryPower inventoryPower) {

        Set<Integer> slots = getSlots(data);

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
                if (itemAction != null) itemAction.accept(new Pair<>(entity.world, itemStack));

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
                    if (itemAction != null) itemAction.accept(new Pair<>(entity.world, itemStack));

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
        if (thrower instanceof PlayerEntity playerEntity && playerEntity.world.isClient) playerEntity.swingHand(Hand.MAIN_HAND);

        double yOffset = thrower.getEyeY() - 0.30000001192092896D;
        ItemEntity itemEntity = new ItemEntity(thrower.world, thrower.getX(), yOffset, thrower.getZ(), itemStack);
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

        thrower.world.spawnEntity(itemEntity);

    }

}
