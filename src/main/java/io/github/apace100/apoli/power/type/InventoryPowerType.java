package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.ApoliContainerTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.data.container.ContainerType;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings("unused")
public class InventoryPowerType extends PowerType implements Active, Inventory {

    public static final TypedDataObjectFactory<InventoryPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("title", ApoliDataTypes.DEFAULT_TRANSLATABLE_TEXT, Text.translatable("container.inventory"))
            .add("container_type", ApoliDataTypes.CONTAINER_TYPE, ApoliContainerTypes.DROPPER)
            .add("drop_on_death_filter", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Key())
            .add("drop_on_death", SerializableDataTypes.BOOLEAN, false)
            .add("recoverable", SerializableDataTypes.BOOLEAN, true),
        (data, condition) -> new InventoryPowerType(
            data.get("title"),
            data.get("container_type"),
            data.get("drop_on_death_filter"),
            data.get("key"),
            data.get("drop_on_death"),
            data.get("recoverable"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("title", powerType.containerTitle)
            .set("container_type", powerType.containerType)
            .set("drop_on_death_filter", powerType.dropOnDeathFilter)
            .set("key", powerType.getKey())
            .set("drop_on_death", powerType.shouldDropOnDeath)
            .set("recoverable", powerType.recoverable)
    );

    private final Text containerTitle;
    private final ContainerType containerType;

    private final Optional<ItemCondition> dropOnDeathFilter;
    private final Key key;

    private final boolean shouldDropOnDeath;
    private final boolean recoverable;

    private final ScreenHandlerFactory containerHandlerFactory;
    private final DefaultedList<ItemStack> container;

    private boolean dirty;

    public InventoryPowerType(Text containerTitle, ContainerType containerType, Optional<ItemCondition> dropOnDeathFilter, Key key, boolean shouldDropOnDeath, boolean recoverable, Optional<EntityCondition> condition) {
        super(condition);
        this.containerTitle = containerTitle;
        this.containerType = containerType;
        this.dropOnDeathFilter = dropOnDeathFilter;
        this.key = key;
        this.shouldDropOnDeath = shouldDropOnDeath;
        this.recoverable = recoverable;
        this.containerHandlerFactory = containerType.create(this);
        this.container = DefaultedList.ofSize(containerType.size(), ItemStack.EMPTY);
        this.setTicking(true);
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.INVENTORY;
    }

    @Override
    public void onLost() {

        if (recoverable) {
            dropItemsOnLost();
        }

    }

    @Override
    public void onUse() {

        if (this.isActive() && getHolder() instanceof PlayerEntity player) {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(containerHandlerFactory, containerTitle));
        }

    }

    @Override
    public void serverTick() {

        if (dirty) {
            PowerHolderComponent.syncPower(getHolder(), getPower());
        }

        this.dirty = false;

    }

    @Override
    public NbtCompound toTag() {

        NbtCompound tag = new NbtCompound();
        Inventories.writeNbt(tag, container, getHolder().getRegistryManager());

        return tag;

    }

    @Override
    public void fromTag(NbtElement tag) {

        if (!(tag instanceof NbtCompound rootNbt)) {
            return;
        }

        this.clear();
        Inventories.readNbt(rootNbt, container, getHolder().getRegistryManager());

    }

    @Override
    public int size() {
        return container.size();
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
        return player == getHolder();
    }

    @Override
    public void clear() {
        this.container.clear();
        this.markDirty();
    }

    @Override
    public Key getKey() {
        return key;
    }

    public DefaultedList<ItemStack> getContainer() {
        return container;
    }

    public Text getContainerTitle() {
        return containerTitle;
    }

    public ScreenHandlerFactory getContainerHandlerFactory() {
        return containerHandlerFactory;
    }

    public boolean shouldDropOnDeath() {
        return shouldDropOnDeath;
    }

    public boolean shouldDropOnDeath(ItemStack stack) {
        return shouldDropOnDeath()
            && dropOnDeathFilter.map(condition -> condition.test(getHolder().getWorld(), stack)).orElse(true);
    }

    public void dropItemsOnDeath() {

        if (!(getHolder() instanceof PlayerEntity playerEntity)) {
            return;
        }

        for (int i = 0; i < container.size(); ++i) {

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

        if (!(getHolder() instanceof PlayerEntity playerEntity)) {
            return;
        }

        for (int i = 0; i < container.size(); ++i) {
            playerEntity.getInventory().offerOrDrop(this.getStack(i));
        }

    }

}
