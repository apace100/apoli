package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.StackReference;

public class EquippedItemActionEntityActionType extends EntityActionType {

    public static final DataObjectFactory<EquippedItemActionEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("equipment_slot", SerializableDataTypes.ATTRIBUTE_MODIFIER_SLOT)
            .add("item_action", ItemAction.DATA_TYPE),
        data -> new EquippedItemActionEntityActionType(
            data.get("equipment_slot"),
            data.get("item_action")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("equipment_slot", actionType.equipmentSlot)
            .set("item_action", actionType.itemAction)
    );

    private final AttributeModifierSlot equipmentSlot;
    private final ItemAction itemAction;

    public EquippedItemActionEntityActionType(AttributeModifierSlot equipmentSlot, ItemAction itemAction) {
        this.equipmentSlot = equipmentSlot;
        this.itemAction = itemAction;
    }

    @Override
    protected void execute(Entity entity) {

        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {

            if (equipmentSlot.matches(slot)) {
                itemAction.execute(entity.getWorld(), StackReference.of(livingEntity, slot));
            }

        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.EQUIPPED_ITEM_ACTION;
    }

}
