package io.github.apace100.apoli.util;

import io.github.apace100.apoli.Apoli;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

import java.util.*;

public final class StackPowerUtil {

    public static void addPower(ItemStack stack, EquipmentSlot slot, Identifier powerId) {
        addPower(stack, slot, powerId, false, false);
    }

    public static void addPower(ItemStack stack, EquipmentSlot slot, Identifier powerId, boolean isHidden, boolean isNegative) {
        addPower(stack, EnumSet.of(slot), powerId, null, isHidden, isNegative);
    }

    public static void addPower(ItemStack stack, EnumSet<EquipmentSlot> slots, Identifier powerId, boolean isHidden, boolean isNegative) {
        addPower(stack, slots, powerId, null, isHidden, isNegative);
    }

    public static void addPower(ItemStack stack, EnumSet<EquipmentSlot> slots, Identifier powerId, NbtElement powerData, boolean isHidden, boolean isNegative) {

        StackPower stackPower = new StackPower();

        stackPower.slots = slots;
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

            removePower(anotherStack, stackPower.slots, stackPower.powerId);
            addPower(anotherStack, stackPower);

        });
    }

    public static void removePower(ItemStack stack, EnumSet<EquipmentSlot> slots, Identifier powerId) {
        for (EquipmentSlot slot : slots) {
            if (removePower(stack, slot, powerId)) {
                break;
            }
        }
    }

    public static boolean removePower(ItemStack stack, EquipmentSlot slot, Identifier powerId) {

        NbtCompound stackNbt = stack.getOrCreateNbt();
        if (!stackNbt.contains("Powers")) {
            return false;
        }

        if (!(stackNbt.get("Powers") instanceof NbtList stackPowersNbt) || (!stackPowersNbt.isEmpty() && stackPowersNbt.getHeldType() != NbtElement.COMPOUND_TYPE)) {
            Apoli.LOGGER.warn("Cannot remove power " + powerId + " from item stack " + stack + ", as it contains conflicting NBT data.");
            return false;
        }

        stackPowersNbt.removeIf(nbtElement -> StackPower.fromNbt((NbtCompound) nbtElement).equals(powerId, slot));
        return true;

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
            if (stackPower.slots.contains(slot)) {
                stackPowers.add(stackPower);
            }
        }

        return stackPowers;

    }

    public static class StackPower {

        public EnumSet<EquipmentSlot> slots;
        public Identifier powerId;
        public NbtElement powerData;

        public boolean isHidden;
        public boolean isNegative;

        public NbtCompound toNbt() {

            NbtCompound nbt = new NbtCompound();
            NbtList slotsNbt = new NbtList();

            for (EquipmentSlot slot : slots) {
                slotsNbt.add(NbtString.of(slot.getName()));
            }

            nbt.put("Slots", slotsNbt);
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
            EnumSet<EquipmentSlot> slots = EnumSet.noneOf(EquipmentSlot.class);

            if (nbt.contains("Slot") && nbt.get("Slot") instanceof NbtString slotNbt) {
                slots.add(EquipmentSlot.byName(slotNbt.asString()));
            }

            if (nbt.contains("Slots") && nbt.get("Slots") instanceof NbtList slotsNbt && (!slotsNbt.isEmpty() && slotsNbt.getHeldType() == NbtElement.STRING_TYPE)) {
                for (int i = 0; i < slotsNbt.size(); i++) {
                    EquipmentSlot slot = EquipmentSlot.byName(slotsNbt.getString(i));
                    slots.add(slot);
                }
            }

            stackPower.slots = slots;
            stackPower.powerId = new Identifier(nbt.getString("Power"));
            stackPower.powerData = nbt.contains("Data") ? nbt.get("Data") : null;
            stackPower.isHidden = nbt.contains("Hidden") && nbt.getBoolean("Hidden");
            stackPower.isNegative = nbt.contains("Negative") && nbt.getBoolean("Negative");

            return stackPower;

        }

        public boolean equals(Identifier powerId, EquipmentSlot... slots) {
            return equals(powerId, EnumSet.copyOf(Arrays.asList(slots)));
        }

        public boolean equals(Identifier powerId, EnumSet<EquipmentSlot> slots) {
            return this.powerId.equals(powerId)
                && this.slots.containsAll(slots);
        }

        @Override
        public boolean equals(Object o) {
            return this == o
                || (o instanceof StackPower other && equals(other.powerId, other.slots));
        }

    }
}
