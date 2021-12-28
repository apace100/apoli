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
    private final DefaultedList<ItemStack> inventory;
    private final TranslatableText containerName;
    private final ScreenHandlerFactory factory;
    private final boolean shouldDropOnDeath;
    private final Predicate<ItemStack> dropOnDeathFilter;

    private boolean lostPower;

    public InventoryPower(PowerType<?> type, LivingEntity entity, String containerName, int size, boolean shouldDropOnDeath, Predicate<ItemStack> dropOnDeathFilter) {
        super(type, entity);
        this.size = size;
        this.inventory = DefaultedList.ofSize(size, ItemStack.EMPTY);
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
        Inventories.writeNbt(tag, inventory);
        return tag;
    }

    @Override
    public void fromTag(NbtElement tag) {
        Inventories.readNbt((NbtCompound)tag, inventory);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return inventory.get(slot).split(amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack stack = inventory.get(slot);
        setStack(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
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

    public boolean shouldDropOnDeath() {
        return shouldDropOnDeath;
    }

    public boolean shouldDropOnDeath(ItemStack stack) {
        return shouldDropOnDeath && dropOnDeathFilter.test(stack);
    }

    //  Drop the cached item stacks of the power
    //  (used for losing the power and death (if shouldDropOnDeath is true))
    public void dropContents() {
        for (int i = 0; i < size; ++i) {
            PlayerEntity player = (PlayerEntity)entity;
            ItemStack currentItemStack = inventory.get(i);
            if (!lostPower && shouldDropOnDeath) {
                if (shouldDropOnDeath(currentItemStack)) {
                    if (!currentItemStack.isEmpty() && EnchantmentHelper.hasVanishingCurse(currentItemStack)) {
                        inventory.set(i, ItemStack.EMPTY);
                    }
                    else {
                        player.dropItem(currentItemStack, true, false);
                        inventory.set(i, ItemStack.EMPTY);
                    }
                }
            }
            else {
                player.dropItem(currentItemStack, true, false);
                inventory.set(i, ItemStack.EMPTY);
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
