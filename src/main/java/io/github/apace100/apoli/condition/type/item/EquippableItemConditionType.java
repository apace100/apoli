package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Optional;

/**
 *  TODO: Use {@link SerializableDataTypes#ATTRIBUTE_MODIFIER_SLOT} for the {@code equipment_slot} field -eggohito
 */
public class EquippableItemConditionType extends ItemConditionType {

    public static final DataObjectFactory<EquippableItemConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT.optional(), Optional.empty()),
        data -> new EquippableItemConditionType(
            data.get("equipment_slot")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("equipment_slot", conditionType.equipmentSlot)
    );

    private final Optional<EquipmentSlot> equipmentSlot;

    public EquippableItemConditionType(Optional<EquipmentSlot> equipmentSlot) {
        this.equipmentSlot = equipmentSlot;
    }

    @Override
    public boolean test(World world, ItemStack stack) {
        Equipment equipment = Equipment.fromStack(stack);
        return equipment != null
            && equipmentSlot.map(equipment.getSlotType()::equals).orElse(true);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return ItemConditionTypes.EQUIPPABLE;
    }

}
