package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.Predicate;

/**
 *  TODO: Use {@link SerializableDataTypes#ATTRIBUTE_MODIFIER_SLOT} for the {@code equipment_slot} field -eggohito
 */
public class EquippedConditionType {

    public static boolean condition(Entity entity, Predicate<Pair<World, ItemStack>> itemCondition, EquipmentSlot equipmentSlot) {
        return entity instanceof LivingEntity livingEntity
            && itemCondition.test(new Pair<>(livingEntity.getWorld(), livingEntity.getEquippedStack(equipmentSlot)));
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("equipped_item"),
            new SerializableData()
                .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION),
            (data, entity) -> condition(entity,
                data.get("item_condition"),
                data.get("equipment_slot")
            )
        );
    }

}
