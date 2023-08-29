package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.DynamicContainerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.InventoryUtil;
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
import net.minecraft.screen.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

import java.util.function.Function;
import java.util.function.Predicate;

public class InventoryPower extends Power implements Active, Inventory {

    private final DefaultedList<ItemStack> container;
    private final Text containerTitle;
    private final ScreenHandlerFactory containerScreen;
    private final Predicate<ItemStack> dropOnDeathFilter;

    private final boolean shouldDropOnDeath;
    private final boolean recoverable;
    private final int containerSize;

    public InventoryPower(PowerType<?> type, LivingEntity entity, String containerTitle, ContainerType containerType, boolean shouldDropOnDeath, Predicate<ItemStack> dropOnDeathFilter, boolean recoverable) {
        this(type, entity, Text.translatable(containerTitle), containerType.getDynamicType(), shouldDropOnDeath, dropOnDeathFilter, new Key(), recoverable);
    }

    public InventoryPower(PowerType<?> powerType, LivingEntity livingEntity, Text containerTitle, DynamicContainerType dynamicContainerType, boolean shouldDropOnDeath, Predicate<ItemStack> dropOnDeathFilter, Key key, boolean recoverable) {
        super(powerType, livingEntity);
        this.containerSize = dynamicContainerType.getSize();
        this.containerScreen = dynamicContainerType.create(this);
        this.container = DefaultedList.ofSize(containerSize, ItemStack.EMPTY);
        this.containerTitle = containerTitle;
        this.shouldDropOnDeath = shouldDropOnDeath;
        this.dropOnDeathFilter = dropOnDeathFilter;
        this.key = key;
        this.recoverable = recoverable;
    }

    public enum ContainerType {

        CHEST("chest", name -> new DynamicContainerType(name, 9, 3, (inventory, columns, rows) ->
            (syncId, playerInventory, player) -> GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, inventory)
        )),

        DOUBLE_CHEST("double_chest", name -> new DynamicContainerType(name, 9, 6, (inventory, columns, rows) ->
            (syncId, playerInventory, player) -> GenericContainerScreenHandler.createGeneric9x6(syncId, playerInventory, inventory)
        )),

        DROPPER("dropper", name -> new DynamicContainerType(name, 3, 3, (inventory, columns, rows) ->
            (syncId, playerInventory, player) -> new Generic3x3ContainerScreenHandler(syncId, playerInventory, inventory)
        )),

        DISPENSER("dispenser", name -> new DynamicContainerType(name, 3, 3, (inventory, columns, rows) ->
            (syncId, playerInventory, player) -> new Generic3x3ContainerScreenHandler(syncId, playerInventory, inventory)
        )),

        HOPPER("hopper", name -> new DynamicContainerType(name, 5, 1, (inventory, columns, rows) ->
            (syncId, playerInventory, player) -> new HopperScreenHandler(syncId, playerInventory, inventory)
        ));

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
        if (entity instanceof PlayerEntity playerEntity && !playerEntity.getWorld().isClient && isActive()) {
            playerEntity.openHandledScreen(new SimpleNamedScreenHandlerFactory(containerScreen, containerTitle));
        }
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        Inventories.writeNbt(tag, container);
        return tag;
    }

    @Override
    public void fromTag(NbtElement tag) {
        Inventories.readNbt((NbtCompound)tag, container);
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

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return container.get(slot).split(amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack stack = container.get(slot);
        setStack(slot, ItemStack.EMPTY);
        return stack;
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

    @SuppressWarnings("unused")
    public DefaultedList<ItemStack> getContainer() {
        return container;
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
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Key())
                .add("recoverable", SerializableDataTypes.BOOLEAN, true),
            data -> (powerType, livingEntity) -> new InventoryPower(
                powerType,
                livingEntity,
                data.get("title"),
                data.get("container_type"),
                data.get("drop_on_death"),
                data.get("drop_on_death_filter"),
                data.get("key"),
                data.get("recoverable")
            )
        ).allowCondition();
    }
}
