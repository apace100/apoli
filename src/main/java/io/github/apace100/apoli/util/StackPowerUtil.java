package io.github.apace100.apoli.util;

import io.github.apace100.apoli.Apoli;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
        CompoundTag nbt = stack.getOrCreateTag();
        ListTag list;
        if(nbt.contains("Powers")) {
            Tag elem = nbt.get("Powers");
            if(elem.getType() != NbtType.LIST) {
                Apoli.LOGGER.warn("Can't add power " + stackPower.powerId + " to item stack "
                    + stack + ", as it contains conflicting NBT data.");
                return;
            }
            list = (ListTag) elem;
        } else {
            list = new ListTag();
            nbt.put("Powers", list);
        }
        list.add(stackPower.toNbt());
    }

    public static List<StackPower> getPowers(ItemStack stack, EquipmentSlot slot) {
        CompoundTag nbt = stack.getOrCreateTag();
        ListTag list;
        List<StackPower> powers = new LinkedList<>();
        if(stack.getItem() instanceof PowerGrantingItem) {
            PowerGrantingItem pgi = (PowerGrantingItem) stack.getItem();
            powers.addAll(pgi.getPowers(stack, slot));
        }
        if(nbt != null && nbt.contains("Powers")) {
            Tag elem = nbt.get("Powers");
            if(elem.getType() != NbtType.LIST) {
                return new ArrayList<>();
            }
            list = (ListTag) elem;
            list.stream().map(p -> {
                if(p.getType() == NbtType.COMPOUND) {
                    return StackPower.fromNbt((CompoundTag) p);
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

        public CompoundTag toNbt() {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("Slot", slot.getName());
            nbt.putString("Power", powerId.toString());
            nbt.putBoolean("Hidden", isHidden);
            nbt.putBoolean("Negative", isNegative);
            return nbt;
        }

        public static StackPower fromNbt(CompoundTag nbt) {
            StackPower stackPower = new StackPower();
            stackPower.slot = EquipmentSlot.byName(nbt.getString("Slot"));
            stackPower.powerId = new Identifier(nbt.getString("Power"));
            stackPower.isHidden = nbt.contains("Hidden") && nbt.getBoolean("Hidden");
            stackPower.isNegative = nbt.contains("Negative") && nbt.getBoolean("Negative");
            return stackPower;
        }
    }
}
