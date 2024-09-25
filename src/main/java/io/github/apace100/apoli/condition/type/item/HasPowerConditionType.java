package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.item.ApoliDataComponentTypes;
import io.github.apace100.apoli.component.item.ItemPowersComponent;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.Optional;

public class HasPowerConditionType {

    public static boolean condition(ItemStack stack, Optional<AttributeModifierSlot> modifierSlot, Identifier powerId) {
        return stack.getOrDefault(ApoliDataComponentTypes.POWERS, ItemPowersComponent.DEFAULT)
            .stream()
            .anyMatch(entry -> entry.powerId().equals(powerId)
                && modifierSlot.map(entry.slot()::equals).orElse(true));
    }

    public static ConditionTypeFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("has_power"),
            new SerializableData()
                .add("slot", SerializableDataTypes.ATTRIBUTE_MODIFIER_SLOT.optional(), Optional.empty())
                .add("power", SerializableDataTypes.IDENTIFIER),
            (data, worldAndStack) -> condition(worldAndStack.getRight(),
                data.get("slot"),
                data.get("power")
            )
        );
    }

}
