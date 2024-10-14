package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;

/**
 *  TODO: Use {@link SerializableDataTypes#ATTRIBUTE_MODIFIER_SLOT} for the {@code equipment_slot} field -eggohito
 */
public class EquippedItemEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<EquippedItemEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("item_condition", ItemCondition.DATA_TYPE)
            .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT),
        data -> new EquippedItemEntityConditionType(
            data.get("item_condition"),
            data.get("equipment_slot")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("item_condition", conditionType.itemCondition)
            .set("equipment_slot", conditionType.equipmentSlot)
    );

    private final ItemCondition itemCondition;
    private final EquipmentSlot equipmentSlot;

    public EquippedItemEntityConditionType(ItemCondition itemCondition, EquipmentSlot equipmentSlot) {
        this.itemCondition = AbstractCondition.setPowerType(itemCondition, getPowerType());
        this.equipmentSlot = equipmentSlot;
    }

    @Override
    public boolean test(Entity entity) {
        return entity instanceof LivingEntity livingEntity
            && itemCondition.test(livingEntity.getWorld(), livingEntity.getEquippedStack(equipmentSlot));
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.EQUIPPED_ITEM;
    }

}
