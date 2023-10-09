package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.StackPowerUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class HasPowerCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {

        ItemStack stack = worldAndStack.getRight();
        Identifier powerId = data.get("power");

        if (data.isPresent("slot")) {
            return hasMatchingStackPowers(powerId, stack, data.get("slot"));
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (hasMatchingStackPowers(powerId, stack, slot)) {
                return true;
            }
        }

        return false;

    }

    private static boolean hasMatchingStackPowers(Identifier powerId, ItemStack stack, EquipmentSlot slot) {
        return StackPowerUtil.getPowers(stack, slot)
            .stream()
            .anyMatch(sp -> sp.powerId.equals(powerId));
    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("has_power"),
            new SerializableData()
                .add("slot", SerializableDataTypes.EQUIPMENT_SLOT, null)
                .add("power", SerializableDataTypes.IDENTIFIER),
            HasPowerCondition::condition
        );
    }

}
