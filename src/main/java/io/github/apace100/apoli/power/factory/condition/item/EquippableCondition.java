package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.IdentifierAlias;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class EquippableCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {

        ItemStack stack = worldAndStack.getRight();
        EquipmentSlot equipmentSlot = data.get("equipment_slot");

        if (equipmentSlot == null) {
            return Equipment.fromStack(stack) != null;
        }

        return LivingEntity.getPreferredEquipmentSlot(stack) == equipmentSlot;

    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
        IdentifierAlias.addPathAlias("is_equippable", "equippable");
        return new ConditionFactory<>(
            Apoli.identifier("equippable"),
            new SerializableData()
                .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT, null),
            EquippableCondition::condition
        );
    }

}
