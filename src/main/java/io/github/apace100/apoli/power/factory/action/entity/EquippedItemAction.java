package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class EquippedItemAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        EquipmentSlot slot = data.get("equipment_slot");
        Consumer<Pair<World, StackReference>> itemAction = data.get("action");

        StackReference stackReference = StackReference.of(livingEntity, slot);
        itemAction.accept(new Pair<>(entity.getWorld(), stackReference));

    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("equipped_item_action"),
            new SerializableData()
                .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT)
                .add("action", ApoliDataTypes.ITEM_ACTION),
            EquippedItemAction::action
        );
    }

}
