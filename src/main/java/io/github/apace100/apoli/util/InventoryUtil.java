package io.github.apace100.apoli.util;

import com.google.common.collect.AbstractIterator;
import com.mojang.datafixers.util.Either;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.mixin.SlotRangesAccessor;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.PowerType;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SlotRange;
import net.minecraft.inventory.SlotRanges;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

public class InventoryUtil {

    public enum InventoryType {
        INVENTORY,
        POWER
    }

    public enum ProcessMode implements ToIntFunction<ItemStack> {

        STACKS {

            @Override
            public int applyAsInt(ItemStack value) {
                return value.isEmpty() ? 0 : 1;
            }

        },

        ITEMS {

            @Override
            public int applyAsInt(ItemStack value) {
                return value.getCount();
            }

        }

    }

    public static void throwItem(Entity thrower, ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
        throwItem(thrower, stack, throwRandomly, retainOwnership, 40);
    }

    public static void throwItem(Entity thrower, ItemStack stack, boolean throwRandomly, boolean retainOwnership, int pickupDelay) {
        throwItem(thrower, stack, throwRandomly, retainOwnership, item -> item.setPickupDelay(pickupDelay));
    }

    public static void throwItem(Entity thrower, ItemStack stack, boolean throwRandomly, boolean retainOwnership, Consumer<ItemEntity> postProcessor) {

        if (stack.isEmpty()) {
            return;
        }

        if (thrower instanceof PlayerEntity playerEntity && playerEntity.getWorld().isClient) {
            playerEntity.swingHand(Hand.MAIN_HAND);
        }

        double yOffset = thrower.getEyeY() - 0.30000001192092896D;

        ItemEntity itemEntity = new ItemEntity(thrower.getWorld(), thrower.getX(), yOffset, thrower.getZ(), stack);
        Random random = Random.create();

        float f;
        float g;

        if (retainOwnership) {
            itemEntity.setThrower(thrower);
        }

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
        postProcessor.accept(itemEntity);

    }

    public static void forEachStack(Entity entity, Consumer<ItemStack> stackConsumer) {

        PowerHolderComponent powerComponent = PowerHolderComponent.getNullable(entity);
        OptionalInt slotToSkip = getSelectedHotBarSlot(entity);

        for (int slot : getAllSlots()) {

            if (slotToSkip.isPresent() && slotToSkip.getAsInt() == slot) {
                continue;
            }

            StackReference stackReference = entity.getStackReference(slot);
            ItemStack stack = stackReference.get();

            if (!stack.isEmpty()) {
                stackConsumer.accept(stack);
            }

        }

        if (powerComponent == null) {
            return;
        }

        for (var type : powerComponent.getPowerTypes()) {

	        if (!(type instanceof Inventory inventory)) {
                continue;
	        }

            for (int i = 0; i < inventory.size(); i++) {

                ItemStack stack = inventory.getStack(i);

                if (!stack.isEmpty()) {
                    stackConsumer.accept(stack);
                }

            }

        }

    }

    public static StackReference getStackReferenceFromStack(Entity entity, ItemStack stack) {
        return getStackReferenceFromStack(entity, stack, (provStack, refStack) -> provStack == refStack);
    }

    public static StackReference getStackReferenceFromStack(Entity entity, ItemStack stack, BiPredicate<ItemStack, ItemStack> equalityPredicate) {

        OptionalInt slotToSkip = getSelectedHotBarSlot(entity);
        for (int slot : getAllSlots()) {

            if (slotToSkip.isPresent() && slotToSkip.getAsInt() == slot) {
                continue;
            }

            StackReference stackReference = entity.getStackReference(slot);
            if (stackReference != StackReference.EMPTY && equalityPredicate.test(stack, stackReference.get())) {
                return stackReference;
            }

        }

        return StackReference.EMPTY;

    }

    /**
     *      <p>For players, their selected hotbar slot will overlap with the `weapon.mainhand` slot reference. This
     *      method returns the slot ID of the selected hotbar slot.</p>
     *
     *      @param entity   The entity to get the slot ID of its selected hotbar slot
     *      @return         The slot ID of the hotbar slot or {@link Integer#MIN_VALUE} if the entity is not a player
     */
    public static OptionalInt getSelectedHotBarSlot(Entity entity) {

        SlotRange slotRange = entity instanceof PlayerEntity player
            ? SlotRanges.fromName("hotbar." + player.getInventory().selectedSlot)
            : null;

        return slotRange != null
            ? OptionalInt.of(slotRange.getSlotIds().getFirst())
            : OptionalInt.empty();

    }

    public static boolean isSlotWithinInventoryBounds(Inventory inventory, int slot) {
        return slot >= 0
            && slot < inventory.size();
    }

    public static StackReference getStackReference(Either<Inventory, Entity> source, int slot) {
        return source.map(inventory -> getInventoryStackReference(inventory, slot), entity -> entity.getStackReference(slot));
    }

    public static StackReference getInventoryStackReference(Inventory inventory, int slot) {
        return isSlotWithinInventoryBounds(inventory, slot)
            ? StackReference.of(inventory, slot)
            : StackReference.EMPTY;
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

    private static final IntSet ALL_SLOTS = new IntOpenHashSet();

    public static IntSet getAllSlots() {

        if (ALL_SLOTS.isEmpty()) {

            for (SlotRange slotRange : SlotRangesAccessor.getSlotRanges()) {
                ALL_SLOTS.addAll(slotRange.getSlotIds());
            }

        }

        return ALL_SLOTS;

    }

    public static OptionalInt getSlotFromStackReference(Entity entity, StackReference stackReference) {

        for (int slot : getAllSlots()) {

            StackReference queriedStackRef = entity.getStackReference(slot);

            if (queriedStackRef != StackReference.EMPTY && queriedStackRef.equals(stackReference)) {
                return OptionalInt.of(slot);
            }

        }

        return OptionalInt.empty();

    }

    public static List<SlotRange> singleOrAllSlots(Optional<SlotRange> slot) {
        return slot
            .map(List::of)
            .orElseGet(SlotRangesAccessor::getSlotRanges);
    }

    public static Iterable<Inventory> getPowerInventories(Entity entity, PowerReference reference) {

        if (reference != null) {
            return () -> new AbstractIterator<>() {

                boolean done;

	            @Override
	            protected Inventory computeNext() {

                    if (!done) {

                        this.done = true;

                        if (reference.getNullablePowerType(entity) instanceof Inventory inventory) {
                            return inventory;
                        }

                    }

                    return endOfData();

	            }

            };
        }

        else {
            return () -> new AbstractIterator<>() {

	            final Iterator<PowerType> types = PowerHolderComponent.getPowerTypes(entity).iterator();

	            @Override
	            protected Inventory computeNext() {

		            while (types.hasNext()) {

			            if (types.next() instanceof Inventory inventory) {
				            return inventory;
			            }

		            }

		            return endOfData();

	            }

            };
        }

    }

}
