package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
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

import java.util.function.Predicate;

public class InventoryPower extends Power implements Active, Inventory {

    private final DefaultedList<ItemStack> container;
    private final MutableText containerTitle;
    private final ScreenHandlerFactory containerScreen;
    private final Predicate<ItemStack> dropOnDeathFilter;

    private final boolean shouldDropOnDeath;
    private final boolean recoverable;
    private final int containerSize;

    public InventoryPower(PowerType<?> type, LivingEntity entity, String containerTitle, ContainerType containerType, boolean shouldDropOnDeath, Predicate<ItemStack> dropOnDeathFilter, boolean recoverable) {
        super(type, entity);
        switch (containerType) {
            case DOUBLE_CHEST:
                containerSize = 54;
                this.containerScreen = (i, playerInventory, playerEntity) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, i,
                    playerInventory, this, 6);
                break;
            case CHEST:
                containerSize = 27;
                this.containerScreen = (i, playerInventory, playerEntity) -> new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X3, i,
                    playerInventory, this, 3);
                break;
            case HOPPER:
                containerSize = 5;
                this.containerScreen = (i, playerInventory, playerEntity) -> new HopperScreenHandler(i, playerInventory, this);
                break;
            case DROPPER, DISPENSER:
            default:
                containerSize = 9;
                this.containerScreen = (i, playerInventory, playerEntity) -> new Generic3x3ContainerScreenHandler(i, playerInventory, this);
                break;
        }
        this.container = DefaultedList.ofSize(containerSize, ItemStack.EMPTY);
        this.containerTitle = Text.translatable(containerTitle);
        this.shouldDropOnDeath = shouldDropOnDeath;
        this.dropOnDeathFilter = dropOnDeathFilter;
        this.recoverable = recoverable;
    }

    public enum ContainerType {
        CHEST,
        DOUBLE_CHEST,
        DROPPER,
        DISPENSER,
        HOPPER
    }

    @Override
    public void onLost() {
        if (recoverable) dropItemsOnLost();
    }

    @Override
    public void onUse() {
        if(!isActive()) {
            return;
        }
        if(!entity.getWorld().isClient && entity instanceof PlayerEntity playerEntity) {
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

    public DefaultedList<ItemStack> getContainer() {
        return container;
    }

    public MutableText getContainerTitle() {
        return containerTitle;
    }

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
        PlayerEntity playerEntity = (PlayerEntity) entity;
        for (int i = 0; i < containerSize; ++i) {
            ItemStack currentItemStack = getStack(i);
            if (shouldDropOnDeath(currentItemStack)) {
                if (!currentItemStack.isEmpty() && EnchantmentHelper.hasVanishingCurse(currentItemStack)) removeStack(i);
                else {
                    playerEntity.dropItem(currentItemStack, true, false);
                    setStack(i, ItemStack.EMPTY);
                }
            }
        }
    }

    public void dropItemsOnLost() {
        PlayerEntity playerEntity = (PlayerEntity) entity;
        for (int i = 0; i < containerSize; ++i) {
            ItemStack currentItemStack = getStack(i);
            playerEntity.getInventory().offerOrDrop(currentItemStack);
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
                .add("container_type", SerializableDataType.enumValue(ContainerType.class), ContainerType.DROPPER)
                .add("drop_on_death", SerializableDataTypes.BOOLEAN, false)
                .add("drop_on_death_filter", ApoliDataTypes.ITEM_CONDITION, null)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key())
                .add("recoverable", SerializableDataTypes.BOOLEAN, true),
            data ->
                (powerType, livingEntity) -> {
                    InventoryPower inventoryPower = new InventoryPower(
                        powerType,
                        livingEntity,
                        data.getString("title"),
                        data.get("container_type"),
                        data.getBoolean("drop_on_death"),
                        data.isPresent("drop_on_death_filter") ? data.get("drop_on_death_filter") : itemStack -> true,
                        data.getBoolean("recoverable")
                    );
                    inventoryPower.setKey(data.get("key"));
                    return inventoryPower;
                })
            .allowCondition();
    }
}
