package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 *  TODO: Use {@link SerializableDataTypes#ATTRIBUTE_MODIFIER_SLOT} for the {@code equipment_slot} field -eggohito
 */
public class EquippableConditionType {

    public static boolean condition(ItemStack stack, @Nullable EquipmentSlot slot) {
        Equipment equipment = Equipment.fromStack(stack);
        return equipment != null
            && (slot == null || slot == equipment.getSlotType());
    }

    public static ConditionTypeFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("equippable"),
            new SerializableData()
                .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT, null),
            (data, worldAndStack) -> condition(worldAndStack.getRight(),
                data.get("equipment_slot")
            )
        );
    }

}
