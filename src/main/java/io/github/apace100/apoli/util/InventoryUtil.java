package io.github.apace100.apoli.util;

import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.mixin.SlotRangesAccessor;
import io.github.apace100.apoli.power.type.InventoryPowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.ArgumentWrapper;
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
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        data.<ArgumentWrapper<Integer>>ifPresent("slot", iaw -> slots.add(iaw.argument()));
        data.<List<ArgumentWrapper<Integer>>>ifPresent("slots", iaws -> slots.addAll(iaws.stream().map(ArgumentWrapper::argument).toList()));

        if (slots.isEmpty()) {
            slots.addAll(getAllSlots());
        }

        return slots;

    }

    public static int checkInventory(Entity entity, Collection<Integer> slots, Optional<InventoryPowerType> inventoryPowerType, Optional<ItemCondition> itemCondition, ProcessMode processMode) {

        Set<Integer> slotSet = prepSlots(slots, entity, inventoryPowerType);
        int matches = 0;

        for (int slot : slotSet) {

            StackReference stackReference = getStackReference(entity, inventoryPowerType, slot);
            ItemStack stack = stackReference.get();

            if (itemCondition.map(condition -> condition.test(entity.getWorld(), stack)).orElse(true)) {
                matches += processMode.getProcessor().apply(stack);
            }

        }

        return matches;

    }

    public static void modifyInventory(SerializableData.Instance data, Entity entity, InventoryPowerType inventoryPower, Function<ItemStack, Integer> processor, int limit) {

        if(limit <= 0) {
            limit = Integer.MAX_VALUE;
        }

        Set<Integer> slots = getSlots(data);
        deduplicateSlots(entity, slots);

        Consumer<Entity> entityAction = data.get("entity_action");
        Predicate<Pair<World, ItemStack>> itemCondition = data.get("item_condition");
        ActionTypeFactory<Pair<World, StackReference>>.Instance itemAction = data.get("item_action");

        int processedItems = 0;
        slots.removeIf(slot -> slotNotWithinBounds(entity, inventoryPower, slot));

        modifyingItemsLoop:
        for (int slot : slots) {

            StackReference stack = getStackReference(entity, inventoryPower, slot);
            if (!(itemCondition == null || itemCondition.test(new Pair<>(entity.getWorld(), stack.get())))) {
                continue;
            }

            int amount = processor.apply(stack.get());
            for (int i = 0; i < amount; i++) {

                if (entityAction != null) {
                    entityAction.accept(entity);
                }

                itemAction.accept(new Pair<>(entity.getWorld(), stack));
                ++processedItems;

                if (processedItems >= limit) {
                    break modifyingItemsLoop;
                }

            }

        }

    }

    public static void replaceInventory(SerializableData.Instance data, Entity entity, InventoryPowerType inventoryPower) {

        Set<Integer> slots = getSlots(data);
        deduplicateSlots(entity, slots);

        Consumer<Entity> entityAction = data.get("entity_action");
        Predicate<Pair<World, ItemStack>> itemCondition = data.get("item_condition");
        Consumer<Pair<World, StackReference>> itemAction = data.get("item_action");

        ItemStack replacementStack = data.get("stack");
        boolean mergeNbt = data.getBoolean("merge_nbt");

        slots.removeIf(slot -> slotNotWithinBounds(entity, inventoryPower, slot));
        for (int slot : slots) {

            StackReference stackReference = getStackReference(entity, inventoryPower, slot);
            ItemStack stack = stackReference.get();

            if (!(itemCondition == null || itemCondition.test(new Pair<>(entity.getWorld(), stack)))) {
                continue;
            }

            if (entityAction != null) {
                entityAction.accept(entity);
            }

            ItemStack stackAfterReplacement = replacementStack.copy();
            if (mergeNbt) {
                //  TODO: Either keep this, or re-implement it to merge components in a possibly hacky way (I'd rather not though) -eggohito
                NbtCompound orgNbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt();
                NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stackAfterReplacement, repNbt -> repNbt.copyFrom(orgNbt));
            }

            stackReference.set(stackAfterReplacement);
            if (itemAction != null) {
                itemAction.accept(new Pair<>(entity.getWorld(), stackReference));
            }

        }

    }

    public static void dropInventory(SerializableData.Instance data, Entity entity, InventoryPowerType inventoryPower) {

        Set<Integer> slots = getSlots(data);
        deduplicateSlots(entity, slots);

        int amount = data.getInt("amount");
        boolean throwRandomly = data.getBoolean("throw_randomly");
        boolean retainOwnership = data.getBoolean("retain_ownership");

        Consumer<Entity> entityAction = data.get("entity_action");
        Predicate<Pair<World, ItemStack>> itemCondition = data.get("item_condition");
        Consumer<Pair<World, StackReference>> itemAction = data.get("item_action");

        slots.removeIf(slot -> slotNotWithinBounds(entity, inventoryPower, slot));
        for (int slot : slots) {

            StackReference stack = getStackReference(entity, inventoryPower, slot);
            if (stack.get().isEmpty() || !(itemCondition == null || itemCondition.test(new Pair<>(entity.getWorld(), stack.get())))) {
                continue;
            }

            if (entityAction != null) {
                entityAction.accept(entity);
            }

            if (itemAction != null) {
                itemAction.accept(new Pair<>(entity.getWorld(), stack));
            }

            ItemStack newStack = stack.get();
            ItemStack droppedStack = ItemStack.EMPTY;
            if (amount != 0) {
                int newAmount = amount < 0 ? amount * -1 : amount;
                droppedStack = newStack.split(newAmount);
            }

            throwItem(entity, droppedStack.isEmpty() ? stack.get() : droppedStack, throwRandomly, retainOwnership);
            stack.set(droppedStack.isEmpty() ? ItemStack.EMPTY : newStack);

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
     *      <p>Check whether the specified slot is <b>not</b> within the bounds of the entity's {@linkplain
     *      StackReference stack reference} or the specified {@link InventoryPowerType}.</p>
     *
     *      @param entity           The entity check the bounds of its {@linkplain StackReference stack reference}
     *      @param inventoryPower   The {@link InventoryPowerType} to check the bounds of
     *      @param slot             The slot
     *      @return                 {@code true} if the slot is within the bounds of the {@linkplain
     *      StackReference stack reference} or the {@link InventoryPowerType}
     */
    public static boolean slotNotWithinBounds(Entity entity, @Nullable InventoryPowerType inventoryPower, int slot) {
        return inventoryPower == null ? entity.getStackReference(slot) == StackReference.EMPTY
            : slot < 0 || slot >= inventoryPower.size();
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

    /**
     *      <p>Get the stack reference from the entity or frin the inventory of the specified {@link InventoryPowerType} (if it's not null).</p>
     *
     *      <p><b>Make sure to only call this method after you filter out the slots that aren't within the bounds
     *      of the entity's {@linkplain StackReference stack reference} or {@link InventoryPowerType} using {@link
     *      #slotNotWithinBounds(Entity, InventoryPowerType, int)}</b></p>
     *
     *      @param entity            The entity to get the item stack from its {@linkplain StackReference stack reference}
     *      @param inventoryPower    The {@link InventoryPowerType} to get the item stack from (can be null)
     *      @param slot              The (numerical) slot to get the item stack from
     *      @return                  The stack reference of the specified slot
     */
    public static StackReference getStackReference(Entity entity, @Nullable InventoryPowerType inventoryPower, int slot) {
        return inventoryPower == null ? entity.getStackReference(slot) : StackReference.of(inventoryPower, slot);
    }

    public static StackReference getStackReference(@NotNull Entity entity, Optional<InventoryPowerType> inventoryPowerType, int slot) {
        return inventoryPowerType
            .map(powerType -> StackReference.of(powerType, slot))
            .orElseGet(() -> entity.getStackReference(slot));
    }

    /**
     *      <p>Get the item stack from the entity's {@linkplain StackReference stack reference} or the inventory of
     *      the specified {@link InventoryPowerType} (if it's not null).</p>
     *
     *      <p><b>Make sure to only call this method after you filter out the slots that aren't within the bounds
     *      of the entity's {@linkplain StackReference stack reference} or {@link InventoryPowerType} using {@link
     *      #slotNotWithinBounds(Entity, InventoryPowerType, int)}</b></p>
     *
     *      @param entity            The entity to get the item stack from its {@linkplain StackReference stack reference}
     *      @param inventoryPower    The {@link InventoryPowerType} to get the item stack from (can be null)
     *      @param slot              The (numerical) slot to get the item stack from
     *      @return                  The item stack from the specified slot
     */
    @Deprecated(forRemoval = true)
    public static ItemStack getStack(Entity entity, @Nullable InventoryPowerType inventoryPower, int slot) {
        return inventoryPower == null ? entity.getStackReference(slot).get() : inventoryPower.getStack(slot);
    }

    /**
     *      <p>Set the item stack on the specified slot of the entity's {@linkplain StackReference stack reference}
     *      or the inventory of the specified {@link InventoryPowerType} (if it's not null).</p>
     *
     *      <p><b>Make sure to only call this method after you filter out the slots that aren't within the bounds
     *      of the entity's {@linkplain StackReference stack reference} or {@link InventoryPowerType} using {@link
     *      #slotNotWithinBounds(Entity, InventoryPowerType, int)}</b></p>
     *
     *      @param entity           The entity to modify the {@linkplain StackReference stack reference} of
     *      @param inventoryPower   The {@link InventoryPowerType} to set the item stack to (can be null)
     *      @param stack            The item stack to set to the specified slot
     *      @param slot             The (numerical) slot to set the item stack to
     */
    @Deprecated(forRemoval = true)
    public static void setStack(Entity entity, InventoryPowerType inventoryPower, ItemStack stack, int slot) {
        if (inventoryPower == null) {
            entity.getStackReference(slot).set(stack);
        } else {
            inventoryPower.setStack(slot, stack);
        }
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
