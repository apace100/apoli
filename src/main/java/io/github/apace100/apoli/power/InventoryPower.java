package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;

import java.util.function.Predicate;

public class InventoryPower extends Power implements Active, Inventory {

    private final int containerSize;
    private final DefaultedList<ItemStack> containerContents;
    private final ScreenHandlerFactory containerFactory;
    private final TranslatableText containerName;
    private final boolean shouldDropOnDeath;
    private final Predicate<ItemStack> dropOnDeathFilter;

    public InventoryPower(PowerType<?> type, LivingEntity entity, String containerName, int containerRows, int containerColumns, boolean shouldDropOnDeath, Predicate<ItemStack> dropOnDeathFilter) {
        super(type, entity);
        this.containerFactory = (i, playerInventory, playerEntity) -> new Generic3x3ContainerScreenHandler(i, playerInventory, this);
        this.containerSize = containerRows * containerColumns;
        this.containerName = new TranslatableText(containerName);
        this.containerContents = DefaultedList.ofSize(containerSize, ItemStack.EMPTY);
        this.shouldDropOnDeath = shouldDropOnDeath;
        this.dropOnDeathFilter = dropOnDeathFilter;
    }

    @Override
    public void onUse() {
        if(!isActive()) {
            return;
        }
        if(!entity.world.isClient && entity instanceof PlayerEntity player) {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(containerFactory, containerName));
        }
    }

    @Override
    public void onLost() {
        dropContents("onLost");
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        Inventories.writeNbt(tag, containerContents);
        return tag;
    }

    @Override
    public void fromTag(NbtElement tag) {
        Inventories.readNbt((NbtCompound)tag, containerContents);
    }

    @Override
    public int size() {
        return containerSize;
    }

    @Override
    public boolean isEmpty() {
        return containerContents.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return containerContents.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return containerContents.get(slot).split(amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack stack = containerContents.get(slot);
        setStack(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        containerContents.set(slot, stack);
    }

    @Override
    public void markDirty() {

    }

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

    /**
     * Appends the provided item stack to the player's inventory
     */
    public void appendStack(ItemStack stack) {
        PlayerEntity player = (PlayerEntity) entity;
        PlayerInventory playerInventory = player.getInventory();
        playerInventory.insertStack(stack);
    }

    /**
     * Appends the provided item stack to the player's inventory unless the player's inventory is full. If the player's inventory is full, it'll
     * drop the item on the ground instead (as if the player has thrown the item).
     */
    public void giveStack(ItemStack stack, boolean withVanishingCurse) {
        if (isInventoryPlayerFull()) {
            dropStack(stack, false, true, withVanishingCurse);
        }
        else {
            appendStack(stack);
        }
    }

    /**
     * Drops the provided item stack on the ground.
     */
    public void dropStack(ItemStack stack, boolean throwRandomly, boolean retainOwnership, boolean withVanishingCurse) {
        PlayerEntity player = (PlayerEntity) entity;
        if (withVanishingCurse) {
            player.dropItem(stack, throwRandomly, retainOwnership);
        }
        else if (!EnchantmentHelper.hasVanishingCurse(stack)) {
            player.dropItem(stack, throwRandomly, retainOwnership);
        }
    }

    public boolean shouldDropOnDeath(ItemStack stack) {
        return shouldDropOnDeath && dropOnDeathFilter.test(stack);
    }

    /**
     * Determines if the player's inventory is full.
     */
    public boolean isInventoryPlayerFull() {
        int inventoryPlayerOccupiedSlots = 0;
        PlayerEntity player = (PlayerEntity) entity;
        PlayerInventory inventoryPlayer = player.getInventory();
        int inventoryPlayerSlots = player.getInventory().main.size();
        for (int i = 0; i < inventoryPlayerSlots; ++i) {
            ItemStack currentItemStack = inventoryPlayer.getStack(i);
            if (!currentItemStack.isEmpty()) {
                inventoryPlayerOccupiedSlots++;
            }
        }
        return inventoryPlayerOccupiedSlots >= inventoryPlayerSlots;
    }

    /**
     * Drop the cached contents of the power upon call
     */
    public void dropContents(String event) {
        switch (event) {
            case "onDeath":
                if (shouldDropOnDeath) {
                    for (int i = 0; i < containerSize; ++i) {
                        ItemStack currentItemStack = getStack(i);
                        if (shouldDropOnDeath(currentItemStack)) {
                            dropStack(currentItemStack, true, false, false);
                            removeStack(i);
                        }
                    }
                }
                break;
            case "onLost":
                for (int i = 0; i < containerSize; ++i) {
                    ItemStack currentItemStack = getStack(i);
                    giveStack(currentItemStack, true);
                    removeStack(i);
                }
                break;
            //  If `event` is neither "onLost" or "onDeath", drop the cached item stacks in a random fashion
            default:
                for (int i = 0; i < containerSize; ++i) {
                    ItemStack currentItemStack = getStack(i);
                    dropStack(currentItemStack, true, false, true);
                    removeStack(i);
                }
                break;
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
        return new PowerFactory<>(Apoli.identifier("inventory"),
            new SerializableData()
                .add("title", SerializableDataTypes.STRING, "container.inventory")
                .add("drop_on_death", SerializableDataTypes.BOOLEAN, false)
                .add("drop_on_death_filter", ApoliDataTypes.ITEM_CONDITION, null)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
            data ->
                (type, player) -> {
                    InventoryPower power = new InventoryPower(
                        type,
                        player,
                        data.getString("title"),
                        3,
                        3,
                        data.getBoolean("drop_on_death"),
                        data.isPresent("drop_on_death_filter") ? data.get("drop_on_death_filter") : itemStack -> true);
                    power.setKey(data.get("key"));
                    return power;
                })
            .allowCondition();
    }
}
