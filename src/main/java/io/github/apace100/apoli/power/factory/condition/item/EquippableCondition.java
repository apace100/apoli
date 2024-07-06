package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.power.factory.condition.ItemConditions;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class EquippableCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {

        ItemStack stack = worldAndStack.getRight();
        Equipment equipment = Equipment.fromStack(stack);

        EquipmentSlot equipmentSlot = data.get("equipment_slot");
        return (equipmentSlot == null && equipment != null)
            || (equipment != null && equipment.getSlotType() == equipmentSlot);

    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {

        ConditionFactory<Pair<World, ItemStack>> factory = new ConditionFactory<>(
            Apoli.identifier("equippable"),
            new SerializableData()
                .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT, null),
            EquippableCondition::condition
        );

        ItemConditions.ALIASES.addPathAlias("is_equippable", factory.getSerializerId().getPath());
        return factory;

    }

}
