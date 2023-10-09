package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.IdentifierAlias;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class EquippableCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {
        return LivingEntity.getPreferredEquipmentSlot(worldAndStack.getRight()) == data.get("equipment_slot");
    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
        IdentifierAlias.addPathAlias("is_equippable", "equippable");
        return new ConditionFactory<>(
            Apoli.identifier("equippable"),
            new SerializableData()
                .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT),
            EquippableCondition::condition
        );
    }

}
