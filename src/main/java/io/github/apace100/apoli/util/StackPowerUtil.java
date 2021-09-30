package io.github.apace100.apoli.util;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class StackPowerUtil {

    public static void addPower(ItemStack stack, EquipmentSlot slot, Identifier powerId) {
        addPower(stack, slot, powerId, false, false);
    }

    public static void addPower(ItemStack stack, EquipmentSlot slot, Identifier powerId, boolean isHidden, boolean isNegative) {
        StackPower stackPower = new StackPower();
        stackPower.slot = slot;
        stackPower.powerId = powerId;
        stackPower.isHidden = isHidden;
        stackPower.isNegative = isNegative;
        addPower(stack, stackPower);
    }

    public static void addPower(ItemStack stack, StackPower stackPower) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList list;
        if(nbt.contains("Powers")) {
            NbtElement elem = nbt.get("Powers");
            if(elem.getType() != NbtType.LIST) {
                Apoli.LOGGER.warn("Can't add power " + stackPower.powerId + " to item stack "
                    + stack + ", as it contains conflicting NBT data.");
                return;
            }
            list = (NbtList)elem;
        } else {
            list = new NbtList();
            nbt.put("Powers", list);
        }
        list.add(stackPower.toNbt());
    }

    public static List<StackPower> getPowers(ItemStack stack, EquipmentSlot slot) {
        NbtCompound nbt = stack.getNbt();
        NbtList list;
        List<StackPower> powers = new LinkedList<>();
        if(stack.getItem() instanceof PowerGrantingItem pgi) {
            powers.addAll(pgi.getPowers(stack, slot));
        }
        if(nbt != null && nbt.contains("Powers")) {
            NbtElement elem = nbt.get("Powers");
            if(elem.getType() != NbtType.LIST) {
                return List.of();
            }
            list = (NbtList)elem;
            list.stream().map(p -> {
                if(p.getType() == NbtType.COMPOUND) {
                    return StackPower.fromNbt((NbtCompound)p);
                } else {
                    Apoli.LOGGER.warn("Invalid power format on stack nbt, stack = " + stack + ", nbt = " + p);
                }
                return null;
            }).filter(sp -> sp != null && sp.slot == slot).forEach(powers::add);
        }
        return powers;
    }

    public static class StackPower {
        public EquipmentSlot slot;
        public Identifier powerId;
        public boolean isHidden;
        public boolean isNegative;

        public NbtCompound toNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("Slot", slot.getName());
            nbt.putString("Power", powerId.toString());
            nbt.putBoolean("Hidden", isHidden);
            nbt.putBoolean("Negative", isNegative);
            return nbt;
        }

        public static StackPower fromNbt(NbtCompound nbt) {
            StackPower stackPower = new StackPower();
            stackPower.slot = EquipmentSlot.byName(nbt.getString("Slot"));
            stackPower.powerId = new Identifier(nbt.getString("Power"));
            stackPower.isHidden = nbt.contains("Hidden") && nbt.getBoolean("Hidden");
            stackPower.isNegative = nbt.contains("Negative") && nbt.getBoolean("Negative");
            return stackPower;
        }
    }
}
