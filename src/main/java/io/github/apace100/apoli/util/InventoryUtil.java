package io.github.apace100.apoli.util;

import io.github.apace100.apoli.mixin.ItemSlotArgumentTypeAccessor;
import io.github.apace100.apoli.power.InventoryPower;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.ArgumentWrapper;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class InventoryUtil {

    public enum InventoryType {
        INVENTORY,
        POWER
    }

    public static void modifyInventory(SerializableData.Instance data, Entity entity, InventoryPower inventoryPower) {

        Set<Integer> slots = new HashSet<>();

        if (data.isPresent("slots")) {
            List<ArgumentWrapper<Integer>> slotArgumentTypes = data.get("slots");
            for (ArgumentWrapper<Integer> slotArgumentType : slotArgumentTypes) {
                slots.add(slotArgumentType.get());
            }
        }

        if (data.isPresent("slot")) {
            ArgumentWrapper<Integer> slotArgumentType = data.get("slot");
            slots.add(slotArgumentType.get());
        }

        if (slots.isEmpty()) {
            ItemSlotArgumentType itemSlotArgumentType = new ItemSlotArgumentType();
            Map<String, Integer> slotNamesWithId = ((ItemSlotArgumentTypeAccessor) itemSlotArgumentType).getSlotNamesToSlotCommandId();
            slots.addAll(slotNamesWithId.values());
        }

        Consumer<Entity> entityAction = data.get("entity_action");
        Predicate<ItemStack> itemCondition = data.get("item_condition");
        ActionFactory<Pair<World, ItemStack>>.Instance itemAction = data.get("item_action");

        if (inventoryPower == null) {
            for (Integer slot : slots) {
                StackReference stackReference = entity.getStackReference(slot);
                if (stackReference != StackReference.EMPTY) {
                    ItemStack currentItemStack = stackReference.get();
                    if (!currentItemStack.isEmpty()) {
                        if (itemCondition == null || itemCondition.test(currentItemStack)) {
                            if (entityAction != null) entityAction.accept(entity);
                            itemAction.accept(new Pair<>(entity.world, currentItemStack));
                        }
                    }
                }
            }
        }

        else {
            slots.removeIf(slot -> slot > inventoryPower.size());
            for (int i = 0; i < inventoryPower.size(); i++) {
                if (!slots.isEmpty() && !slots.contains(i)) continue;
                ItemStack currentItemStack = inventoryPower.getStack(i);
                if (!currentItemStack.isEmpty()) {
                    if (itemCondition == null || itemCondition.test(currentItemStack)) {
                        if (entityAction != null) entityAction.accept(entity);
                        itemAction.accept(new Pair<>(entity.world, currentItemStack));
                    }
                }
            }
        }

    }

    public static void replaceInventory(SerializableData.Instance data, Entity entity, InventoryPower inventoryPower) {

        Set<Integer> slots = new HashSet<>();

        if (data.isPresent("slots")) {
            List<ArgumentWrapper<Integer>> slotArgumentTypes = data.get("slots");
            for (ArgumentWrapper<Integer> slotArgumentType : slotArgumentTypes) {
                slots.add(slotArgumentType.get());
            }
        }

        if (data.isPresent("slot")) {
            ArgumentWrapper<Integer> slotArgumentType = data.get("slot");
            slots.add(slotArgumentType.get());
        }

        if (slots.isEmpty()) {
            ItemSlotArgumentType itemSlotArgumentType = new ItemSlotArgumentType();
            Map<String, Integer> slotNamesWithId = ((ItemSlotArgumentTypeAccessor) itemSlotArgumentType).getSlotNamesToSlotCommandId();
            slots.addAll(slotNamesWithId.values());
        }

        ItemStack replacementStack = data.get("stack");
        Consumer<Entity> entityAction = data.get("entity_action");
        Predicate<ItemStack> itemCondition = data.get("item_condition");
        ActionFactory<Pair<World, ItemStack>>.Instance itemAction = data.get("item_action");

        if (inventoryPower == null) {
            for (Integer slot : slots) {
                StackReference stackReference = entity.getStackReference(slot);
                if (stackReference != StackReference.EMPTY) {
                    ItemStack currentItemStack = stackReference.get();
                    if (itemCondition == null || itemCondition.test(currentItemStack)) {
                        if (entityAction != null) entityAction.accept(entity);
                        stackReference.set(replacementStack.copy());
                        if (itemAction != null) itemAction.accept(new Pair<>(entity.world, currentItemStack));
                    }
                }
            }
        }

        else {
            slots.removeIf(slot -> slot > inventoryPower.size());
            for (int i = 0; i < inventoryPower.size(); i++) {
                if (!slots.isEmpty() && !slots.contains(i)) continue;
                ItemStack currentItemStack = inventoryPower.getStack(i);
                if (itemCondition == null || itemCondition.test(currentItemStack)) {
                    if (entityAction != null) entityAction.accept(entity);
                    inventoryPower.setStack(i, replacementStack.copy());
                    if (itemAction != null) itemAction.accept(new Pair<>(entity.world, currentItemStack));
                }
            }
        }

    }

    public static void dropInventory(SerializableData.Instance data, Entity entity, InventoryPower inventoryPower) {

        Set<Integer> slots = new HashSet<>();

        if (data.isPresent("slots")) {
            List<ArgumentWrapper<Integer>> slotArgumentTypes = data.get("slots");
            for (ArgumentWrapper<Integer> slotArgumentType : slotArgumentTypes) {
                slots.add(slotArgumentType.get());
            }
        }

        if (data.isPresent("slot")) {
            ArgumentWrapper<Integer> slotArgumentType = data.get("slot");
            slots.add(slotArgumentType.get());
        }

        if (slots.isEmpty()) {
            ItemSlotArgumentType itemSlotArgumentType = new ItemSlotArgumentType();
            Map<String, Integer> slotNamesWithId = ((ItemSlotArgumentTypeAccessor) itemSlotArgumentType).getSlotNamesToSlotCommandId();
            slots.addAll(slotNamesWithId.values());
        }

        boolean throwRandomly = data.getBoolean("throw_randomly");
        boolean retainOwnership = data.getBoolean("retain_ownership");

        Consumer<Entity> entityAction = data.get("entity_action");
        Predicate<ItemStack> itemCondition = data.get("item_condition");
        ActionFactory<Pair<World, ItemStack>>.Instance itemAction = data.get("item_action");

        if (inventoryPower == null) {
            for (Integer slot : slots) {
                StackReference stackReference = entity.getStackReference(slot);
                if (stackReference != StackReference.EMPTY) {
                    ItemStack currentItemStack = stackReference.get();
                    if (!currentItemStack.isEmpty()) {
                        if (itemCondition == null || itemCondition.test(currentItemStack)) {
                            if (entityAction != null) entityAction.accept(entity);
                            if (itemAction != null) itemAction.accept(new Pair<>(entity.world, currentItemStack));
                            throwItem(entity, currentItemStack, throwRandomly, retainOwnership);
                            stackReference.set(ItemStack.EMPTY);
                        }
                    }
                }
            }
        }

        else {
            slots.removeIf(slot -> slot > inventoryPower.size());
            for (int i = 0; i < inventoryPower.size(); i++) {
                if (!slots.isEmpty() && !slots.contains(i)) continue;
                ItemStack currentItemStack = inventoryPower.getStack(i);
                if (!currentItemStack.isEmpty()) {
                    if (itemCondition == null || itemCondition.test(currentItemStack)) {
                        if (entityAction != null) entityAction.accept(entity);
                        if (itemAction != null) itemAction.accept(new Pair<>(entity.world, currentItemStack));
                        throwItem(entity, currentItemStack, throwRandomly, retainOwnership);
                        inventoryPower.setStack(i, ItemStack.EMPTY);
                    }
                }
            }
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
                -g * f + 0.1F + (random.nextFloat() - random.nextFloat()) * 0.1F,
                (double) (j * h * f) + Math.sin(k) * (double) l
            );
        }

        thrower.world.spawnEntity(itemEntity);

    }

}
