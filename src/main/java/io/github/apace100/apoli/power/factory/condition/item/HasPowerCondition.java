package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.item.ApoliDataComponentTypes;
import io.github.apace100.apoli.component.item.ItemPowersComponent;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HasPowerCondition {

    public static boolean condition(ItemStack stack, @Nullable AttributeModifierSlot slot, Identifier powerId) {
        return stack.getOrDefault(ApoliDataComponentTypes.POWERS, ItemPowersComponent.DEFAULT)
            .stream()
            .anyMatch(entry -> entry.powerId().equals(powerId)
                && (slot == null || entry.slot().equals(slot)));
    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("has_power"),
            new SerializableData()
                .add("slot", SerializableDataTypes.ATTRIBUTE_MODIFIER_SLOT)
                .add("power", SerializableDataTypes.IDENTIFIER),
            (data, worldAndStack) -> condition(
                worldAndStack.getRight(),
                data.get("slot"),
                data.get("power")
            )
        );
    }

}
