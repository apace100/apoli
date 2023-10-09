package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.StackPowerUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class PowerCountCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {

        Comparison comparison = data.get("comparison");
        int compareTo = data.get("compare_to");
        int total = 0;

        if (data.isPresent("slot")) {
            total = StackPowerUtil.getPowers(worldAndStack.getRight(), data.get("slot")).size();
        } else {

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                total += StackPowerUtil.getPowers(worldAndStack.getRight(), slot).size();
            }

        }

        return comparison.compare(total, compareTo);

    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("power_count"),
            new SerializableData()
                .add("slot", SerializableDataTypes.EQUIPMENT_SLOT, null)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            PowerCountCondition::condition
        );
    }

}
