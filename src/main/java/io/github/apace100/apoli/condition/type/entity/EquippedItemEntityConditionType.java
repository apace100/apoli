package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;

public class EquippedItemEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<EquippedItemEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("item_condition", ItemCondition.DATA_TYPE)
            .add("equipment_slot", SerializableDataTypes.ATTRIBUTE_MODIFIER_SLOT),
        data -> new EquippedItemEntityConditionType(
            data.get("item_condition"),
            data.get("equipment_slot")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("item_condition", conditionType.itemCondition)
            .set("equipment_slot", conditionType.equipmentSlot)
    );

    private final ItemCondition itemCondition;
    private final AttributeModifierSlot equipmentSlot;

    public EquippedItemEntityConditionType(ItemCondition itemCondition, AttributeModifierSlot equipmentSlot) {
        this.itemCondition = itemCondition;
        this.equipmentSlot = equipmentSlot;
    }

    @Override
    public boolean test(Entity entity) {

        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {

            if (equipmentSlot.matches(slot) && itemCondition.test(livingEntity.getWorld(), livingEntity.getEquippedStack(slot))) {
                return true;
            }

        }

        return false;

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.EQUIPPED_ITEM;
    }

}
