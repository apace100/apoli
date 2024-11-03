package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.SlotRange;
import net.minecraft.inventory.SlotRanges;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class KeepInventoryPowerType extends PowerType {

    private static final ObjectOpenHashSet<Integer> DEFAULT_SLOTS = ObjectOpenHashSet.of(SlotRanges.fromName("inventory.*"), SlotRanges.fromName("hotbar.*"), SlotRanges.fromName("armor.*"))
        .stream()
        .map(SlotRange::getSlotIds)
        .map(IntCollection::intStream)
        .flatMap(IntStream::boxed)
        .collect(Collectors.toCollection(ObjectOpenHashSet::new));

    public static final TypedDataObjectFactory<KeepInventoryPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("slots", ApoliDataTypes.SINGLE_SLOT_RANGES.optional(), Optional.empty()),
        (data, condition) -> new KeepInventoryPowerType(
            data.get("item_condition"),
            data.get("slots"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("item_condition", powerType.itemCondition)
            .set("slots", powerType.slotRanges)
    );

    private final Optional<ItemCondition> itemCondition;
    private final Optional<List<SlotRange>> slotRanges;

    private final ObjectOpenHashSet<Integer> slots;
    private final Int2ObjectOpenHashMap<ItemStack> cachedStacks;

    public KeepInventoryPowerType(Optional<ItemCondition> itemCondition, Optional<List<SlotRange>> slotRanges, Optional<EntityCondition> condition) {
        super(condition);

        this.itemCondition = itemCondition;
        this.slotRanges = slotRanges;

        this.slots = new ObjectOpenHashSet<>();
        this.cachedStacks = new Int2ObjectOpenHashMap<>();

        this.slotRanges.stream()
            .flatMap(Collection::stream)
            .map(SlotRange::getSlotIds)
            .map(IntCollection::intStream)
            .flatMap(IntStream::boxed)
            .forEach(this.slots::add);

        this.slots.trim();

    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.KEEP_INVENTORY;
    }

    public void preventItemsFromDropping() {

        LivingEntity holder = getHolder();
        ObjectOpenHashSet<Integer> slots = slotRanges
            .map(slotRanges -> this.slots)
            .orElse(DEFAULT_SLOTS);

        for (int slot : slots) {

            StackReference stackReference = holder.getStackReference(slot);
            ItemStack stack = stackReference.get();

            if (!stack.isEmpty() && itemCondition.map(condition -> condition.test(getHolder().getWorld(), stack)).orElse(true)) {
                cachedStacks.put(slot, stack);
                stackReference.set(ItemStack.EMPTY);
            }

        }

        this.cachedStacks.trim();

    }

    public void restoreSavedItems() {

        if (cachedStacks.isEmpty()) {
            Apoli.LOGGER.warn("Power \"{}\" tried restoring items without having saved any!", getPower().getId());
        }

        else {

            for (Int2ObjectMap.Entry<ItemStack> cachedStackEntry : cachedStacks.int2ObjectEntrySet()) {

                int slot = cachedStackEntry.getIntKey();
                ItemStack cachedStack = cachedStackEntry.getValue();

                if (!cachedStack.isEmpty()) {
                    getHolder().getStackReference(slot).set(cachedStack);
                }

            }

            this.cachedStacks.clear();
            this.cachedStacks.trim();

        }

    }

}
