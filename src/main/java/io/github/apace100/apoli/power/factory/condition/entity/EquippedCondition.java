package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class EquippedCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        Predicate<Pair<World, ItemStack>> itemCondition = data.get("item_condition");
        return entity instanceof LivingEntity livingEntity
            && itemCondition.test(new Pair<>(livingEntity.getWorld(), livingEntity.getEquippedStack(data.get("equipment_slot"))));
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("equipped_item"),
            new SerializableData()
                .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION),
            EquippedCondition::condition
        );
    }

}
