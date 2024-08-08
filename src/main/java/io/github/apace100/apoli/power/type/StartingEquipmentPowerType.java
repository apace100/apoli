package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
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

public class StartingEquipmentPowerType extends PowerType {

    private final List<ItemStack> itemStacks = new LinkedList<>();
    private final HashMap<Integer, ItemStack> slottedStacks = new HashMap<>();
    private boolean recurrent;

    public StartingEquipmentPowerType(Power power, LivingEntity entity) {
        super(power, entity);
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
            if(entity instanceof PlayerEntity player) {
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
            if(entity instanceof PlayerEntity player) {
                player.giveItemStack(copy);
            } else {
                entity.dropStack(copy);
            }
        });
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("starting_equipment"),
            new SerializableData()
                .add("stack", ApoliDataTypes.POSITIONED_ITEM_STACK, null)
                .add("stacks", ApoliDataTypes.POSITIONED_ITEM_STACKS, null)
                .add("recurrent", SerializableDataTypes.BOOLEAN, false),
            data -> (power, entity) -> {

                StartingEquipmentPowerType powerType = new StartingEquipmentPowerType(power, entity);

                if (data.isPresent("stack")) {

                    Pair<Integer, ItemStack> slotAndStack = data.get("stack");

                    ItemStack stack = slotAndStack.getRight();
                    int slot = slotAndStack.getLeft();

                    if(slot > Integer.MIN_VALUE) {
                        powerType.addStack(slot, stack);
                    }

                    else {
                        powerType.addStack(stack);
                    }

                }

                if (data.isPresent("stacks")) {

                    List<Pair<Integer, ItemStack>> stacks = data.get("stacks");
                    stacks.forEach(slotAndStack -> {

                        ItemStack stack = slotAndStack.getRight();
                        int slot = slotAndStack.getLeft();

                        if (slot > Integer.MIN_VALUE) {
                            powerType.addStack(slot, stack);
                        }

                        else {
                            powerType.addStack(stack);
                        }

                    });

                }

                powerType.setRecurrent(data.getBoolean("recurrent"));
                return powerType;

            }
        );
    }

}
