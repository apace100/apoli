package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class StartingEquipmentPower extends Power {

    private final List<ItemStack> itemStacks = new LinkedList<>();
    private final HashMap<Integer, ItemStack> slottedStacks = new HashMap<>();
    private boolean recurrent;

    public StartingEquipmentPower(PowerType type, LivingEntity entity) {
        super(type, entity);
    }

    public void setRecurrent(boolean recurrent) {
        this.recurrent = recurrent;
    }

    public void addStack(ItemStack stack) {
        this.itemStacks.add(stack);
    }

    public void addStack(int slot, ItemStack stack) {
        slottedStacks.put(slot, stack);
    }

    @Override
    public void onGained() {
        giveStacks();
    }

    @Override
    public void onRespawn() {
        if(recurrent) {
            giveStacks();
        }
    }

    private void giveStacks() {
        slottedStacks.forEach((slot, stack) -> {
            if(entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity)entity;
                PlayerInventory inventory = player.getInventory();
                if(inventory.getStack(slot).isEmpty()) {
                    inventory.setStack(slot, stack);
                } else {
                    player.giveItemStack(stack);
                }
            } else {
                entity.dropStack(stack);
            }
        });
        itemStacks.forEach(is -> {
            ItemStack copy = is.copy();
            if(entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity)entity;
                player.giveItemStack(copy);
            } else {
                entity.dropStack(copy);
            }
        });
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("starting_equipment"),
            new SerializableData()
                .add("stack", ApoliDataTypes.POSITIONED_ITEM_STACK, null)
                .add("stacks", ApoliDataTypes.POSITIONED_ITEM_STACKS, null)
                .add("recurrent", SerializableDataTypes.BOOLEAN, false),
            data ->
                (type, player) -> {
                    StartingEquipmentPower power = new StartingEquipmentPower(type, player);
                    if(data.isPresent("stack")) {
                        Pair<Integer, ItemStack> stack = (Pair<Integer, ItemStack>)data.get("stack");
                        int slot = stack.getLeft();
                        if(slot > Integer.MIN_VALUE) {
                            power.addStack(stack.getLeft(), stack.getRight());
                        } else {
                            power.addStack(stack.getRight());
                        }
                    }
                    if(data.isPresent("stacks")) {
                        ((List<Pair<Integer, ItemStack>>)data.get("stacks"))
                            .forEach(integerItemStackPair -> {
                                int slot = integerItemStackPair.getLeft();
                                if(slot > Integer.MIN_VALUE) {
                                    power.addStack(integerItemStackPair.getLeft(), integerItemStackPair.getRight());
                                } else {
                                    power.addStack(integerItemStackPair.getRight());
                                }
                            });
                    }
                    power.setRecurrent(data.getBoolean("recurrent"));
                    return power;
                });
    }
}
