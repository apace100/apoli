package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Optional;

public class EquippableItemConditionType extends ItemConditionType {

    public static final TypedDataObjectFactory<EquippableItemConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("equipment_slot", SerializableDataTypes.ATTRIBUTE_MODIFIER_SLOT.optional(), Optional.empty()),
        data -> new EquippableItemConditionType(
            data.get("equipment_slot")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("equipment_slot", conditionType.equipmentSlot)
    );

    private final Optional<AttributeModifierSlot> equipmentSlot;

    public EquippableItemConditionType(Optional<AttributeModifierSlot> equipmentSlot) {
        this.equipmentSlot = equipmentSlot;
    }

    @Override
    public boolean test(World world, ItemStack stack) {
        Equipment equipment = Equipment.fromStack(stack);
        return equipment != null
            && equipmentSlot.map(slot -> slot.matches(equipment.getSlotType())).orElse(true);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return ItemConditionTypes.EQUIPPABLE;
    }

}
