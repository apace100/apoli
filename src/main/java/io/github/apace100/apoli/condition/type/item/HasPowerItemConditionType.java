package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.component.item.ApoliDataComponentTypes;
import io.github.apace100.apoli.component.item.ItemPowersComponent;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Optional;

public class HasPowerItemConditionType extends ItemConditionType {

    public static final DataObjectFactory<HasPowerItemConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("slot", SerializableDataTypes.ATTRIBUTE_MODIFIER_SLOT.optional(), Optional.empty())
            .add("power", ApoliDataTypes.POWER_REFERENCE),
        data -> new HasPowerItemConditionType(
            data.get("slot"),
            data.get("power")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("slot", conditionType.slot)
            .set("power", conditionType.power)
    );

    private final Optional<AttributeModifierSlot> slot;
    private final PowerReference power;

    public HasPowerItemConditionType(Optional<AttributeModifierSlot> slot, PowerReference power) {
        this.slot = slot;
        this.power = power;
    }

    @Override
    public boolean test(World world, ItemStack stack) {
        return stack.getOrDefault(ApoliDataComponentTypes.POWERS, ItemPowersComponent.DEFAULT)
            .stream()
            .filter(entry -> slot.map(entry.slot()::equals).orElse(true))
            .map(ItemPowersComponent.Entry::powerId)
            .anyMatch(power.getId()::equals);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return ItemConditionTypes.HAS_POWER;
    }

}
