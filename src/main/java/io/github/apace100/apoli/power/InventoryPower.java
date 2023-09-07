package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.DynamicContainerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.slot.SlotBiFilter;
import io.github.apace100.apoli.util.slot.SlotFilter;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class InventoryPower extends Power implements Active, Inventory {

    private final DefaultedList<ItemStack> container;
    private final Text containerTitle;
    private final ScreenHandlerFactory containerScreen;
    private final Predicate<ItemStack> dropOnDeathFilter;
    private final DynamicContainerType specifiedContainerType;

    private final List<SlotBiFilter> insertBiFilters;
    private final boolean insertBiFiltersInclusive;

    private final boolean shouldDropOnDeath;
    private final boolean recoverable;
    private final int containerSize;

    private DynamicContainerType containerType;
    private boolean opened = false;

    public InventoryPower(PowerType<?> type, LivingEntity entity, String containerTitle, ContainerType containerType, boolean shouldDropOnDeath, Predicate<ItemStack> dropOnDeathFilter, boolean recoverable) {
        this(type, entity, Text.translatable(containerTitle), containerType.getDynamicType(), shouldDropOnDeath, dropOnDeathFilter, null, true, new Key(), recoverable);
    }

    public InventoryPower(PowerType<?> powerType, LivingEntity livingEntity, Text containerTitle, DynamicContainerType containerType, boolean shouldDropOnDeath, Predicate<ItemStack> dropOnDeathFilter, List<SlotBiFilter> insertBiFilters, boolean insertBiFiltersInclusive, Key key, boolean recoverable) {
        super(powerType, livingEntity);
        this.specifiedContainerType = containerType;
        this.containerSize = containerType.getSize();
        this.containerScreen = containerType.create(this);
        this.container = DefaultedList.ofSize(containerSize, ItemStack.EMPTY);
        this.containerTitle = containerTitle;
        this.shouldDropOnDeath = shouldDropOnDeath;
        this.dropOnDeathFilter = dropOnDeathFilter;
        this.insertBiFilters = insertBiFilters != null ? insertBiFilters : new LinkedList<>();
        this.insertBiFiltersInclusive = insertBiFiltersInclusive;
        this.key = key;
        this.recoverable = recoverable;
    }

    public enum ContainerType {

        CHEST("chest", name -> DynamicContainerType.of(name, 9, 3)),
        DOUBLE_CHEST("double_chest", name -> DynamicContainerType.of(name, 9, 6)),
        DROPPER("dropper", name -> DynamicContainerType.of(name, 3, 3)),
        DISPENSER("dispenser", name -> DynamicContainerType.of(name, 3, 3)),
        HOPPER("hopper", name -> DynamicContainerType.of(name, 5, 1));

        private final String name;
        private final DynamicContainerType dynamicContainerType;

        ContainerType(String name, Function<String, DynamicContainerType> factory) {
            this.name = name;
            this.dynamicContainerType = factory.apply(name);
        }

        public String getName() {
            return name;
        }

        public DynamicContainerType getDynamicType() {
            return dynamicContainerType;
        }

    }

    @Override
    public void onLost() {
        if (recoverable) {
            dropItemsOnLost();
        }
    }

    @Override
    public void onUse() {

        if (PowerHolderComponent.hasPower(entity, InventoryPower.class, InventoryPower::isOpened)) {
            return;
        }

        if (entity instanceof PlayerEntity playerEntity && !playerEntity.getWorld().isClient && isActive()) {
            playerEntity.openHandledScreen(new SimpleNamedScreenHandlerFactory(containerScreen, containerTitle));
        }

    }

    @Override
    public NbtCompound toTag() {

        NbtCompound rootNbt = new NbtCompound();

        Inventories.writeNbt(rootNbt, container);
        rootNbt.putBoolean("Opened", opened);
        rootNbt.put("ContainerType", specifiedContainerType.toNbt());

        return rootNbt;

    }

    @Override
    public void fromTag(NbtElement tag) {

        if (!(tag instanceof NbtCompound rootNbt)) {
            return;
        }

        Inventories.readNbt(rootNbt, container);
        opened = rootNbt.getBoolean("Opened");
        containerType = DynamicContainerType.fromNbt(rootNbt.getCompound("ContainerType"));

    }

    @Override
    public int size() {
        return containerSize;
    }

    @Override
    public boolean isEmpty() {
        return container.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return container.get(slot);
    }

    @Nullable
    public ItemStack getNullableStack(Integer slot) {
        return slot == null || slot < 0 || slot >= size() ? null : getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return getStack(slot).split(amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        setStack(slot, ItemStack.EMPTY);
        return getStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        container.set(slot, stack);
    }

    @Override
    public void markDirty() {}

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return player == this.entity;
    }

    @Override
    public void clear() {
        for(int i = 0; i < containerSize; i++) {
            setStack(i, ItemStack.EMPTY);
        }
    }

    @Override
    public void onOpen(PlayerEntity player) {
        opened = true;
        PowerHolderComponent.syncPower(player, this.getType());
    }

    @Override
    public void onClose(PlayerEntity player) {
        opened = false;
        PowerHolderComponent.syncPower(player, this.getType());
    }

    public boolean isOpened() {
        return opened;
    }

    @SuppressWarnings("unused")
    public DefaultedList<ItemStack> getContainer() {
        return container;
    }

    public DynamicContainerType getContainerType() {
        return containerType;
    }

    @SuppressWarnings("unused")
    public MutableText getContainerTitle() {
        return containerTitle.copy();
    }

    @SuppressWarnings("unused")
    public ScreenHandlerFactory getContainerScreen() {
        return containerScreen;
    }

    public boolean shouldDropOnDeath() {
        return shouldDropOnDeath;
    }

    public boolean shouldDropOnDeath(ItemStack stack) {
        return shouldDropOnDeath && dropOnDeathFilter.test(stack);
    }

    public void dropItemsOnDeath() {
        for (int i = 0; i < containerSize; i++) {

            ItemStack currentStack = getStack(i);
            if (!shouldDropOnDeath(currentStack)) {
                continue;
            }

            removeStack(i);
            if (!EnchantmentHelper.hasVanishingCurse(currentStack)) {
                InventoryUtil.throwItem(entity, currentStack, true, false);
            }

        }
    }

    public void dropItemsOnLost() {
        for (int i = 0; i < containerSize; i++) {
            ItemStack currentStack = getStack(i);
            if (entity instanceof PlayerEntity playerEntity) {
                playerEntity.getInventory().offerOrDrop(currentStack);
            } else {
                InventoryUtil.throwItem(entity, currentStack, false, false);
            }
        }
    }

    private Key key;

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        this.key = key;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("inventory"),
            new SerializableData()
                .add("title", SerializableDataTypes.TEXT, Text.translatable("container.inventory"))
                .add("container_type", ApoliDataTypes.BACKWARDS_COMPATIBLE_CONTAINER_TYPE, ContainerType.DROPPER.getDynamicType())
                .add("drop_on_death", SerializableDataTypes.BOOLEAN, false)
                .add("drop_on_death_filter", ApoliDataTypes.ITEM_CONDITION, null)
                .add("insert_bifilter", ApoliDataTypes.SLOT_BIFILTERS, null)
                .add("insert_bifilter_inclusive", SerializableDataTypes.BOOLEAN, true)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Key())
                .add("recoverable", SerializableDataTypes.BOOLEAN, true),
            data -> (powerType, livingEntity) -> new InventoryPower(
                powerType,
                livingEntity,
                data.get("title"),
                data.get("container_type"),
                data.get("drop_on_death"),
                data.get("drop_on_death_filter"),
                data.get("insert_bifilter"),
                data.get("insert_bifilter_inclusive"),
                data.get("key"),
                data.get("recoverable")
            )
        ).allowCondition();
    }

    public class FilteredSlot extends Slot {

        private final int index;
        public FilteredSlot(int index, int x, int y) {
            super(InventoryPower.this, index, x, y);
            this.index = index;
        }

        @Override
        public boolean canInsert(ItemStack stack) {

            if (insertBiFilters.isEmpty()) {
                return true;
            }

            Stream<SlotBiFilter> matchingBiFilters = insertBiFilters
                .stream()
                .filter(biFilter -> biFilter.testSlot(biFilter.getLeft(), index));

            if (insertBiFiltersInclusive) {
                return matchingBiFilters
                    .allMatch(slotBiFilter -> testBiFilter(slotBiFilter, stack));
            } else {
                return matchingBiFilters
                    .anyMatch(slotBiFilter -> testBiFilter(slotBiFilter, stack));
            }

        }

        private boolean testBiFilter(SlotBiFilter slotBiFilter, ItemStack insertingStack) {

            SlotFilter insertingToSlotFilter = slotBiFilter.getLeft();
            SlotFilter onSlotFilter = slotBiFilter.getRight();

            Integer onSlot = onSlotFilter != null ? (onSlotFilter.slot() != null ? (onSlotFilter.slot()) : null) : null;
            ItemStack onSlotStack = InventoryPower.this.getNullableStack(onSlot);

            if (onSlot == null || (onSlotStack != null && onSlotFilter.testStack(onSlotStack))) {
                return (insertingToSlotFilter == null || insertingToSlotFilter.test(index, insertingStack));
            }

            return true;

        }

    }

}
