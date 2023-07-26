package io.github.apace100.apoli.util;

import io.github.apace100.apoli.Apoli;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;

public final class StackPowerUtil {

    public static void addPower(ItemStack stack, EquipmentSlot slot, Identifier powerId) {
        addPower(stack, slot, powerId, false, false);
    }

    public static void addPower(ItemStack stack, EquipmentSlot slot, Identifier powerId, boolean isHidden, boolean isNegative) {
        addPower(stack, slot, powerId, null, isHidden, isNegative);
    }

    public static void addPower(ItemStack stack, EquipmentSlot slot, Identifier powerId, NbtElement powerData, boolean isHidden, boolean isNegative) {

        StackPower stackPower = new StackPower();

        stackPower.slot = slot;
        stackPower.powerId = powerId;
        stackPower.powerData = powerData;
        stackPower.isHidden = isHidden;
        stackPower.isNegative = isNegative;

        addPower(stack, stackPower);

    }

    public static void addPower(ItemStack stack, StackPower stackPower) {

        NbtCompound stackNbt = stack.getOrCreateNbt();
        if (!stackNbt.contains("Powers")) {
            stackNbt.put("Powers", new NbtList());
        }

        if (!(stackNbt.get("Powers") instanceof NbtList stackPowersNbt) || (!stackPowersNbt.isEmpty() && stackPowersNbt.getHeldType() != NbtElement.COMPOUND_TYPE)) {
            Apoli.LOGGER.warn("Cannot add power " + stackPower.powerId + " to item stack " + stack + ", as it contains conflicting NBT data.");
            return;
        }

        stackPowersNbt.add(stackPower.toNbt());

    }

    public static void updatePower(Entity entity, ItemStack stack, StackPower stackPower) {
        InventoryUtil.forEachStack(entity, anotherStack -> {

            if (!ItemStack.areEqual(anotherStack, stack)) {
                return;
            }

            removePower(anotherStack, stackPower.slot, stackPower.powerId);
            addPower(anotherStack, stackPower);

        });
    }

    public static void removePower(ItemStack stack, EquipmentSlot slot, Identifier powerId) {

        NbtCompound stackNbt = stack.getOrCreateNbt();
        if (!stackNbt.contains("Powers")) {
            return;
        }

        if (!(stackNbt.get("Powers") instanceof NbtList stackPowersNbt) || (!stackPowersNbt.isEmpty() && stackPowersNbt.getHeldType() != NbtElement.COMPOUND_TYPE)) {
            Apoli.LOGGER.warn("Cannot remove power " + powerId + " from item stack " + stack + ", as it contains conflicting NBT data.");
            return;
        }

        stackPowersNbt.removeIf(nbtElement -> StackPower.fromNbt((NbtCompound) nbtElement).equals(powerId, slot));

    }

    public static List<StackPower> getPowers(ItemStack stack, EquipmentSlot slot) {

        NbtCompound stackNbt = stack.getNbt();
        List<StackPower> stackPowers = new LinkedList<>();

        if (stack.getItem() instanceof PowerGrantingItem pgi) {
            stackPowers.addAll(pgi.getPowers(stack, slot));
        }

        if (stackNbt == null || !stackNbt.contains("Powers")) {
            return stackPowers;
        }

        if (!(stackNbt.get("Powers") instanceof NbtList stackPowersNbt) || (!stackPowersNbt.isEmpty() && stackPowersNbt.getHeldType() != NbtElement.COMPOUND_TYPE)) {
            Apoli.LOGGER.error("Invalid power format on stack NBT! Stack = " + stack + ", NBT = " + stackNbt.get("Powers"));
            return stackPowers;
        }

        for (NbtElement stackPowerNbt : stackPowersNbt) {
            StackPower stackPower = StackPower.fromNbt((NbtCompound) stackPowerNbt);
            if (stackPower.slot == slot) {
                stackPowers.add(stackPower);
            }
        }

        return stackPowers;

    }

    public static class StackPower {

        public EquipmentSlot slot;
        public Identifier powerId;
        public NbtElement powerData;

        public boolean isHidden;
        public boolean isNegative;

        public NbtCompound toNbt() {

            NbtCompound nbt = new NbtCompound();

            nbt.putString("Slot", slot.getName());
            nbt.putString("Power", powerId.toString());

            if (powerData != null) {
                nbt.put("Data", powerData);
            }

            nbt.putBoolean("Hidden", isHidden);
            nbt.putBoolean("Negative", isNegative);

            return nbt;

        }

        public static StackPower fromNbt(NbtCompound nbt) {

            StackPower stackPower = new StackPower();

            stackPower.slot = EquipmentSlot.byName(nbt.getString("Slot"));
            stackPower.powerId = new Identifier(nbt.getString("Power"));
            stackPower.powerData = nbt.contains("Data") ? nbt.get("Data") : null;
            stackPower.isHidden = nbt.contains("Hidden") && nbt.getBoolean("Hidden");
            stackPower.isNegative = nbt.contains("Negative") && nbt.getBoolean("Negative");

            return stackPower;

        }

        public boolean equals(Identifier powerId, EquipmentSlot slot) {
            return this.powerId.equals(powerId)
                && this.slot == slot;
        }

        @Override
        public boolean equals(Object o) {
            return this == o
                || (o instanceof StackPower other && equals(other.powerId, other.slot));
        }

    }
}
