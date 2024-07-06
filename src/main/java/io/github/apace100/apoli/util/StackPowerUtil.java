package io.github.apace100.apoli.util;

//  TODO: Convert this into an item component -eggohito
public final class StackPowerUtil {

//    public static void addPower(ItemStack stack, EquipmentSlot slot, Identifier powerId) {
//        addPower(stack, slot, powerId, false, false);
//    }
//
//    public static void addPower(ItemStack stack, EquipmentSlot slot, Identifier powerId, boolean isHidden, boolean isNegative) {
//        StackPower stackPower = new StackPower();
//        stackPower.slot = slot;
//        stackPower.powerId = powerId;
//        stackPower.isHidden = isHidden;
//        stackPower.isNegative = isNegative;
//        addPower(stack, stackPower);
//    }
//
//    public static void addPower(ItemStack stack, StackPower stackPower) {
//        NbtCompound nbt = stack.getOrCreateNbt();
//        NbtList list;
//        if(nbt.contains("Powers")) {
//            NbtElement elem = nbt.get("Powers");
//            if(elem.getType() != NbtType.LIST) {
//                Apoli.LOGGER.warn("Can't add power " + stackPower.powerId + " to item stack "
//                    + stack + ", as it contains conflicting NBT data.");
//                return;
//            }
//            list = (NbtList)elem;
//        } else {
//            list = new NbtList();
//            nbt.put("Powers", list);
//        }
//        list.add(stackPower.toNbt());
//    }
//
//    public static void removePower(ItemStack stack, EquipmentSlot slot, Identifier powerId) {
//        NbtCompound nbt = stack.getOrCreateNbt();
//        NbtList list;
//        if(nbt.contains("Powers")) {
//            NbtElement elem = nbt.get("Powers");
//            if(elem.getType() != NbtType.LIST) {
//                Apoli.LOGGER.warn("Can't remove power " + powerId + " from item stack "
//                    + stack + ", as it contains conflicting NBT data.");
//                return;
//            }
//            list = (NbtList)elem;
//            int found = -1;
//            while(list.size() > 0) {
//                for(int i = 0; i < list.size(); i++) {
//                    StackPower sp = StackPower.fromNbt(list.getCompound(i));
//                    if(sp.powerId.equals(powerId) && sp.slot == slot) {
//                        found = i;
//                        break;
//                    }
//                }
//                if(found >= 0) {
//                    list.remove(found);
//                    found = -1;
//                } else {
//                    break;
//                }
//            }
//        }
//    }
//
//    public static List<StackPower> getPowers(ItemStack stack, EquipmentSlot slot) {
//        NbtCompound nbt = stack.getNbt();
//        NbtList list;
//        List<StackPower> powers = new LinkedList<>();
//        if(stack.getItem() instanceof PowerGrantingItem pgi) {
//            powers.addAll(pgi.getPowers(stack, slot));
//        }
//        if(nbt != null && nbt.contains("Powers")) {
//            NbtElement elem = nbt.get("Powers");
//            if(elem.getType() != NbtType.LIST) {
//                return List.of();
//            }
//            list = (NbtList)elem;
//            list.stream().map(p -> {
//                if(p.getType() == NbtType.COMPOUND) {
//                    return StackPower.fromNbt((NbtCompound)p);
//                } else {
//                    Apoli.LOGGER.warn("Invalid power format on stack nbt, stack = " + stack + ", nbt = " + p);
//                }
//                return null;
//            }).filter(sp -> sp != null && sp.slot == slot).forEach(powers::add);
//        }
//        return powers;
//    }
//
//    public static class StackPower {
//        public EquipmentSlot slot;
//        public Identifier powerId;
//        public boolean isHidden;
//        public boolean isNegative;
//
//        public NbtCompound toNbt() {
//            NbtCompound nbt = new NbtCompound();
//            nbt.putString("Slot", slot.getName());
//            nbt.putString("Power", powerId.toString());
//            nbt.putBoolean("Hidden", isHidden);
//            nbt.putBoolean("Negative", isNegative);
//            return nbt;
//        }
//
//        public static StackPower fromNbt(NbtCompound nbt) {
//            StackPower stackPower = new StackPower();
//            stackPower.slot = EquipmentSlot.byName(nbt.getString("Slot"));
//            stackPower.powerId = new Identifier(nbt.getString("Power"));
//            stackPower.isHidden = nbt.contains("Hidden") && nbt.getBoolean("Hidden");
//            stackPower.isNegative = nbt.contains("Negative") && nbt.getBoolean("Negative");
//            return stackPower;
//        }
//    }
}
