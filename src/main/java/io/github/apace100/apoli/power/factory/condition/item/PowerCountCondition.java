package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.item.ApoliDataComponentTypes;
import io.github.apace100.apoli.component.item.ItemPowersComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PowerCountCondition {

    public static boolean condition(ItemStack stack, @Nullable AttributeModifierSlot slot, Comparison comparison, int compareTo) {

        ItemPowersComponent itemPowers = stack.getOrDefault(ApoliDataComponentTypes.POWERS, ItemPowersComponent.DEFAULT);
        int powers;

        if (slot != null) {
            powers = (int) itemPowers
                .stream()
                .filter(entry -> entry.slot().equals(slot))
                .count();
        }

        else {
            powers = itemPowers.size();
        }

        return comparison.compare(powers, compareTo);

    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("power_count"),
            new SerializableData()
                .add("slot", ApoliDataTypes.ATTRIBUTE_MODIFIER_SLOT, null)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            (data, worldAndStack) -> condition(
                worldAndStack.getRight(),
                data.get("slot"),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
