package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
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

    private final int size;
    private final DefaultedList<ItemStack> inventoryPower;
    private final TranslatableText containerName;
    private final ScreenHandlerFactory factory;
    private final boolean shouldDropOnDeath;
    private final Predicate<ItemStack> dropOnDeathFilter;

    private boolean lostPower;
    private boolean inventoryPlayerFull;

    public InventoryPower(PowerType<?> type, LivingEntity entity, String containerName, int size, boolean shouldDropOnDeath, Predicate<ItemStack> dropOnDeathFilter) {
        super(type, entity);
        this.size = size;
        this.inventoryPower = DefaultedList.ofSize(size, ItemStack.EMPTY);
        this.containerName = new TranslatableText(containerName);
        this.factory = (i, playerInventory, playerEntity) -> {
            return new Generic3x3ContainerScreenHandler(i, playerInventory, this);
        };
        this.shouldDropOnDeath = shouldDropOnDeath;
        this.dropOnDeathFilter = dropOnDeathFilter;
    }

    @Override
    public void onUse() {
        if(!isActive()) {
            return;
        }
        if(!entity.world.isClient && entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(factory, containerName));
        }
    }

    @Override
    public void onLost() {
        lostPower = true;
        dropContents();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        Inventories.writeNbt(tag, inventoryPower);
        return tag;
    }

    @Override
    public void fromTag(NbtElement tag) {
        Inventories.readNbt((NbtCompound)tag, inventoryPower);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return inventoryPower.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventoryPower.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return inventoryPower.get(slot).split(amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack stack = inventoryPower.get(slot);
        setStack(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventoryPower.set(slot, stack);
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
        for(int i = 0; i < size; i++) {
            setStack(i, ItemStack.EMPTY);
        }
    }

    public boolean shouldDropOnDeath(ItemStack stack) {
        return shouldDropOnDeath && dropOnDeathFilter.test(stack);
    }

    //  Count how many player inventory slots are occupied by an item stack
    //  (called to check if the player's inventory is full)
    public void checkInventoryPlayerFull() {
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
        inventoryPlayerFull = inventoryPlayerOccupiedSlots >= inventoryPlayerSlots;
    }

    //  Drop the cached item stacks of the power
    //  (called when the player dies or loses the power)
    public void dropContents() {
        PlayerEntity player = (PlayerEntity) entity;
        if (!lostPower && shouldDropOnDeath) {
            for (int i = 0; i < size; ++i) {
                ItemStack currentItemStack = inventoryPower.get(i);
                if (!currentItemStack.isEmpty() && shouldDropOnDeath(currentItemStack)) {
                    if (!EnchantmentHelper.hasVanishingCurse(currentItemStack)) {
                        player.dropItem(currentItemStack, true, false);
                    }
                    inventoryPower.set(i, ItemStack.EMPTY);
                }
            }
        }
        else if (lostPower) {
            for (int i = 0; i < size; ++i) {
                checkInventoryPlayerFull();
                ItemStack currentItemStack = inventoryPower.get(i);
                if (!currentItemStack.isEmpty()) {
                    if (inventoryPlayerFull) {
                        player.dropItem(currentItemStack, false, false);
                    }
                    else {
                        player.getInventory().insertStack(currentItemStack);
                    }
                    inventoryPower.set(i, ItemStack.EMPTY);
                }
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
        return new PowerFactory<>(Apoli.identifier("inventory"),
            new SerializableData()
                .add("title", SerializableDataTypes.STRING, "container.inventory")
                .add("drop_on_death", SerializableDataTypes.BOOLEAN, false)
                .add("drop_on_death_filter", ApoliDataTypes.ITEM_CONDITION, null)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
            data ->
                (type, player) -> {
                    InventoryPower power = new InventoryPower(type, player, data.getString("title"), 9,
                        data.getBoolean("drop_on_death"),
                        data.isPresent("drop_on_death_filter") ? (ConditionFactory<ItemStack>.Instance) data.get("drop_on_death_filter") :
                            itemStack -> true);
                    power.setKey((Active.Key)data.get("key"));
                    return power;
                })
            .allowCondition();
    }
}
