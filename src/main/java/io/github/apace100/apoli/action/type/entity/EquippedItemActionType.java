package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.Consumer;

/**
 *  TODO: Use {@link SerializableDataTypes#ATTRIBUTE_MODIFIER_SLOT} for the {@code equipment_slot} field and rename the {@code action} field to {@code item_action} -eggohito
 */
public class EquippedItemActionType {

    public static void action(Entity entity, EquipmentSlot slot, Consumer<Pair<World, StackReference>> itemAction) {

        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        StackReference stackReference = StackReference.of(livingEntity, slot);
        itemAction.accept(new Pair<>(entity.getWorld(), stackReference));

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("equipped_item_action"),
            new SerializableData()
                .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT)
                .add("action", ApoliDataTypes.ITEM_ACTION),
            (data, entity) -> action(entity,
                data.get("equipment_slot"),
                data.get("action")
            )
        );
    }

}
