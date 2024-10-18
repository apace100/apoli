package io.github.apace100.apoli.util;

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.mixin.SlotRangesAccessor;
import io.github.apace100.apoli.power.type.InventoryPowerType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SlotRange;
import net.minecraft.inventory.SlotRanges;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InventoryUtil {

    public enum InventoryType {
        INVENTORY,
        POWER
    }

    public enum ProcessMode implements ToIntFunction<ItemStack> {

        STACKS {

            @Override
            public int applyAsInt(ItemStack value) {
                return 1;
            }

        },

        ITEMS {

            @Override
            public int applyAsInt(ItemStack value) {
                return value.getCount();
            }

        }

    }

    public static int checkInventory(Entity entity, Collection<Integer> slots, Optional<InventoryPowerType> inventoryPowerType, Optional<ItemCondition> itemCondition, ProcessMode processMode) {

        Set<Integer> slotSet = prepSlots(slots, entity, inventoryPowerType);
        int matches = 0;

        for (int slot : slotSet) {

            StackReference stackReference = getStackReference(entity, inventoryPowerType, slot);
            ItemStack stack = stackReference.get();

            if (itemCondition.map(condition -> condition.test(entity.getWorld(), stack)).orElse(true)) {
                matches += processMode.applyAsInt(stack);
            }

        }

        return matches;

    }

    public static void modifyInventory(Entity entity, Collection<Integer> slots, Optional<InventoryPowerType> inventoryPowerType, Optional<EntityAction> entityAction, ItemAction itemAction, Optional<ItemCondition> itemCondition, Optional<Integer> limit, ProcessMode processMode) {

        Set<Integer> preppedSlots = prepSlots(slots, entity, inventoryPowerType);
        AtomicInteger processedItems = new AtomicInteger();

        modifyingItemsLoop:
        for (int preppedSlot : preppedSlots) {

            StackReference stackReference = getStackReference(entity, inventoryPowerType, preppedSlot);
            ItemStack stack = stackReference.get();

            if (!itemCondition.map(condition -> condition.test(entity.getWorld(), stack)).orElse(true)) {
                continue;
            }

            int amount = processMode.applyAsInt(stack);
            for (int i = 0; i < amount; i++) {

                entityAction.ifPresent(action -> action.execute(entity));
                itemAction.execute(entity.getWorld(), stackReference);

                if (limit.map(value -> processedItems.incrementAndGet() >= value).orElse(false)) {
                    break modifyingItemsLoop;
                }

            }

        }

    }

    public static void replaceInventory(Entity entity, Collection<Integer> slots, Optional<InventoryPowerType> inventoryPowerType, Optional<EntityAction> entityAction, Optional<ItemAction> itemAction, Optional<ItemCondition> itemCondition, ItemStack replacementStack, boolean mergeNbt) {

        Set<Integer> slotSet = prepSlots(slots, entity, inventoryPowerType);
        for (int slot : slotSet) {

            StackReference stackReference = getStackReference(entity, inventoryPowerType, slot);
            ItemStack stack = stackReference.get();

            if (!itemCondition.map(condition -> condition.test(entity.getWorld(), stack)).orElse(true)) {
                continue;
            }

            ItemStack replacementStackCopy = replacementStack.copy();
            entityAction.ifPresent(action -> action.execute(entity));

            if (mergeNbt) {
                //  TODO: Either keep this as is, or re-implement it to merge components in a possibly hacky way (I'd rather not)   -eggohito
                NbtCompound originalStackNbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt();
                NbtComponent.set(DataComponentTypes.CUSTOM_DATA, replacementStackCopy, replacementStackNbt -> replacementStackNbt.copyFrom(originalStackNbt));
            }

            stackReference.set(replacementStackCopy);
            itemAction.ifPresent(action -> action.execute(entity.getWorld(), stackReference));

        }

    }

    public static void dropInventory(Entity entity, Collection<Integer> slots, Optional<InventoryPowerType> inventoryPowerType, Optional<EntityAction> entityAction, Optional<ItemAction> itemAction, Optional<ItemCondition> itemCondition, boolean throwRandomly, boolean retainOwnership, Optional<Integer> amount) {

        Set<Integer> slotSet = prepSlots(slots, entity, inventoryPowerType);
        for (int slot : slotSet) {

            StackReference stackReference = getStackReference(entity, inventoryPowerType, slot);
            ItemStack stack = stackReference.get();

            if (stack.isEmpty() || !itemCondition.map(condition -> condition.test(entity.getWorld(), stack)).orElse(true)) {
                continue;
            }

            entityAction.ifPresent(action -> action.execute(entity));
            itemAction.ifPresent(action -> action.execute(entity.getWorld(), stackReference));

            ItemStack droppedStack = amount
                .map(Math::abs)
                .map(stack::split)
                .orElse(ItemStack.EMPTY);

            throwItem(entity, droppedStack.isEmpty() ? stack : droppedStack, throwRandomly, retainOwnership);
            stackReference.set(droppedStack.isEmpty() ? ItemStack.EMPTY : stack);

        }

    }

    public static void throwItem(Entity thrower, ItemStack itemStack, boolean throwRandomly, boolean retainOwnership) {

        if (itemStack.isEmpty()) {
            return;
        }

        if (thrower instanceof PlayerEntity playerEntity && playerEntity.getWorld().isClient) {
            playerEntity.swingHand(Hand.MAIN_HAND);
        }

        double yOffset = thrower.getEyeY() - 0.30000001192092896D;
        ItemEntity itemEntity = new ItemEntity(thrower.getWorld(), thrower.getX(), yOffset, thrower.getZ(), itemStack);
        itemEntity.setPickupDelay(40);

        Random random = new Random();

        float f;
        float g;

        if (retainOwnership) itemEntity.setThrower(thrower);
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

    public static void forEachStack(Entity entity, Consumer<ItemStack> itemStackConsumer) {

        int slotToSkip = getDuplicatedSlotIndex(entity);
        for (int slot : getAllSlots()) {

            if (slot == slotToSkip) {
                slotToSkip = Integer.MIN_VALUE;
                continue;
            }

            StackReference stackReference = entity.getStackReference(slot);
            if (stackReference == StackReference.EMPTY) {
                continue;
            }

            ItemStack stack = stackReference.get();
            if (!stack.isEmpty()) {
                itemStackConsumer.accept(stack);
            }

        }

        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(entity).orElse(null);
        if (component == null) {
            return;
        }

        List<InventoryPowerType> inventoryPowers = component.getPowerTypes(InventoryPowerType.class);
        for (InventoryPowerType inventoryPower : inventoryPowers) {
            for (int index = 0; index < inventoryPower.size(); index++) {

                ItemStack stack = inventoryPower.getStack(index);
                if (!stack.isEmpty()) {
                    itemStackConsumer.accept(stack);
                }

            }
        }

    }

    public static StackReference getStackReferenceFromStack(Entity entity, ItemStack stack) {
        return getStackReferenceFromStack(entity, stack, (provStack, refStack) -> provStack == refStack);
    }

    public static StackReference getStackReferenceFromStack(Entity entity, ItemStack stack, BiPredicate<ItemStack, ItemStack> equalityPredicate) {

        int slotToSkip = getDuplicatedSlotIndex(entity);
        for (int slot : getAllSlots()) {

            if (slot == slotToSkip) {
                slotToSkip = Integer.MIN_VALUE;
                continue;
            }

            StackReference stackReference = entity.getStackReference(slot);
            if (stackReference != StackReference.EMPTY && equalityPredicate.test(stack, stackReference.get())) {
                return stackReference;
            }

        }

        return StackReference.EMPTY;

    }

    private static final List<String> EXEMPT_SLOTS = List.of("weapon", "weapon.mainhand");

    private static void deduplicateSlots(Entity entity, Set<Integer> slots) {

        int selectedHotbarSlot = getDuplicatedSlotIndex(entity);
        if (selectedHotbarSlot != Integer.MIN_VALUE && slots.contains(selectedHotbarSlot)) {
            SlotRangesAccessor.getSlotRanges()
                .stream()
                .filter(sr -> EXEMPT_SLOTS.contains(sr.asString()))
                .flatMapToInt(sr -> sr.getSlotIds().intStream())
                .forEach(slots::remove);
        }

    }

    /**
     *      <p>For players, their selected hotbar slot will overlap with the `weapon.mainhand` slot reference. This
     *      method returns the slot ID of the selected hotbar slot.</p>
     *
     *      @param entity   The entity to get the slot ID of its selected hotbar slot
     *      @return         The slot ID of the hotbar slot or {@link Integer#MIN_VALUE} if the entity is not a player
     */
    private static int getDuplicatedSlotIndex(Entity entity) {

        SlotRange slotRange = entity instanceof PlayerEntity player
            ? SlotRanges.fromName("hotbar." + player.getInventory().selectedSlot)
            : null;

        return slotRange != null
            ? slotRange.getSlotIds().getFirst()
            : Integer.MIN_VALUE;

    }

    /**
     *  <p>Checks whether the specified {@code slot} index is within the bounds of the specified {@link InventoryPowerType},
     *  or the entity's {@link StackReference}, in that order.</p>
     *
     *  @param entity               the entity to check the bounds of its {@link StackReference}
     *  @param inventoryPowerType   the {@link InventoryPowerType} to check the bounds of (if present)
     *  @param slot                 the slot index
     *  @return                     {@code true} if the slot index is within the bounds
     */
    public static boolean slotNotWithinBounds(Entity entity, Optional<InventoryPowerType> inventoryPowerType, int slot) {
        return inventoryPowerType
            .map(powerType -> slot < 0 || slot >= powerType.size())
            .orElseGet(() -> entity.getStackReference(slot) == StackReference.EMPTY);
    }

    public static StackReference getStackReference(@NotNull Entity entity, Optional<InventoryPowerType> inventoryPowerType, int slot) {
        return inventoryPowerType
            .map(powerType -> StackReference.of(powerType, slot))
            .orElseGet(() -> entity.getStackReference(slot));
    }

    /**
     *      <p>Creates a stack reference that is not linked to any entity for use with item actions.</p>
     *
     *      <p>Recommended for usage when either you don't have an entity for this operation, or you
     *      don't want to set the entity's StackReference.</p>
     *
     *      @param startingStack The ItemStack that this reference will start with.
     *      @return A {@linkplain StackReference} that contains an ItemStack.
     */
    public static StackReference createStackReference(ItemStack startingStack) {
        return new StackReference() {

            ItemStack stack = startingStack;

            @Override
            public ItemStack get() {
                return stack;
            }

            @Override
            public boolean set(ItemStack stack) {
                this.stack = stack;
                return true;
            }

        };
    }

    public static Set<Integer> getAllSlots() {
        return SlotRangesAccessor.getSlotRanges()
            .stream()
            .flatMapToInt(slotRange -> slotRange.getSlotIds().intStream())
            .boxed()
            .collect(Collectors.toSet());
    }

    public static Set<Integer> prepSlots(Collection<Integer> slots, Entity entity, Optional<InventoryPowerType> inventoryPowerType) {

        Stream<Integer> slotStream = slots.isEmpty()
            ? SlotRangesAccessor.getSlotRanges().stream().flatMapToInt(slotRange -> slotRange.getSlotIds().intStream()).boxed()
            : slots.stream();
        Set<Integer> slotSet = slotStream
            .filter(slot -> slotNotWithinBounds(entity, inventoryPowerType, slot))
            .collect(Collectors.toSet());

        deduplicateSlots(entity, slotSet);
        return slotSet;

    }

    @Nullable
    public static Integer getSlotFromStackReference(Entity entity, StackReference stackReference) {

        for (int slot : getAllSlots()) {

            StackReference queriedStackRef = entity.getStackReference(slot);

            if (queriedStackRef != StackReference.EMPTY && queriedStackRef.equals(stackReference)) {
                return slot;
            }

        }

        return null;

    }

}
