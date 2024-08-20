package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.function.Predicate;

@SuppressWarnings("unused")
public class InventoryPowerType extends PowerType implements Active, Inventory {

    private final DefaultedList<ItemStack> container;
    private final MutableText containerTitle;
    private final ScreenHandlerFactory containerScreen;
    private final Predicate<Pair<World, ItemStack>> dropOnDeathFilter;
    private final Key key;

    private final boolean shouldDropOnDeath;
    private final boolean recoverable;

    private final int containerSize;

    private boolean dirty;

    public InventoryPowerType(Power power, LivingEntity entity, String containerTitle, ContainerType containerType, boolean shouldDropOnDeath, Predicate<Pair<World, ItemStack>> dropOnDeathFilter, Key key, boolean recoverable) {
        super(power, entity);
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
        this.key = key;
        this.recoverable = recoverable;
        this.setTicking(true);
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
        if (recoverable) {
            dropItemsOnLost();
        }
    }

    @Override
    public void onUse() {

        if (this.isActive() && entity instanceof PlayerEntity player) {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(containerScreen, containerTitle));
        }

    }

    @Override
    public void tick() {

        if (dirty) {
            PowerHolderComponent.syncPower(entity, power);
        }

        this.dirty = false;

    }

    @Override
    public NbtCompound toTag() {

        NbtCompound tag = new NbtCompound();
        Inventories.writeNbt(tag, container, entity.getRegistryManager());

        return tag;

    }

    @Override
    public void fromTag(NbtElement tag) {

        if (!(tag instanceof NbtCompound rootNbt)) {
            return;
        }

        this.clear();
        Inventories.readNbt(rootNbt, container, entity.getRegistryManager());

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

        ItemStack stack = Inventories.splitStack(container, slot, amount);
        if (!stack.isEmpty()) {
            this.markDirty();
        }

        return stack;

    }

    @Override
    public ItemStack removeStack(int slot) {

        ItemStack prevStack = this.getStack(slot);
        this.setStack(slot, ItemStack.EMPTY);

        return prevStack;

    }

    @Override
    public void setStack(int slot, ItemStack stack) {

        container.set(slot, stack);
        if (!stack.isEmpty()) {
            stack.setCount(Math.min(stack.getCount(), this.getMaxCountPerStack()));
        }

        this.markDirty();

    }

    @Override
    public void markDirty() {
        this.dirty = true;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return player == this.entity;
    }

    @Override
    public void clear() {
        this.container.clear();
        this.markDirty();
    }

    @Deprecated(forRemoval = true)
    public StackReference getStackReference(int slot) {
        return new StackReference() {

            @Override
            public ItemStack get() {
                return InventoryPowerType.this.getStack(slot);
            }

            @Override
            public boolean set(ItemStack stack) {
                InventoryPowerType.this.setStack(slot, stack);
                return true;
            }

        };
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
        return shouldDropOnDeath
            && (dropOnDeathFilter == null || dropOnDeathFilter.test(new Pair<>(entity.getWorld(), stack)));
    }

    public void dropItemsOnDeath() {

        if (!(entity instanceof PlayerEntity playerEntity)) {
            return;
        }

        for (int i = 0; i < containerSize; ++i) {

            ItemStack currentStack = this.getStack(i).copy();
            if (!this.shouldDropOnDeath(currentStack)) {
                continue;
            }

            this.removeStack(i);
            if (!EnchantmentHelper.hasAnyEnchantmentsWith(currentStack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
                playerEntity.dropItem(currentStack, true, false);
            }

        }

    }

    public void dropItemsOnLost() {

        if (!(entity instanceof PlayerEntity playerEntity)) {
            return;
        }

        for (int i = 0; i < containerSize; ++i) {
            playerEntity.getInventory().offerOrDrop(this.getStack(i));
        }

    }


    @Override
    public Key getKey() {
        return key;
    }


    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("inventory"),
            new SerializableData()
                .add("title", SerializableDataTypes.STRING, "container.inventory")
                .add("container_type", SerializableDataType.enumValue(ContainerType.class), ContainerType.DROPPER)
                .add("drop_on_death", SerializableDataTypes.BOOLEAN, false)
                .add("drop_on_death_filter", ApoliDataTypes.ITEM_CONDITION, null)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key())
                .add("recoverable", SerializableDataTypes.BOOLEAN, true),
            data -> (power, entity) -> new InventoryPowerType(power, entity,
                data.getString("title"),
                data.get("container_type"),
                data.get("drop_on_death"),
                data.get("drop_on_death_filter"),
                data.get("key"),
                data.getBoolean("recoverable")
            )
        ).allowCondition();
    }
}
