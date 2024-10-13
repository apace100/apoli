package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.component.item.ApoliDataComponentTypes;
import io.github.apace100.apoli.component.item.ItemPowersComponent;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Optional;

public class PowerCountItemConditionType extends ItemConditionType {

    public static final DataObjectFactory<PowerCountItemConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("slot", SerializableDataTypes.ATTRIBUTE_MODIFIER_SLOT.optional(), Optional.empty())
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
        data -> new PowerCountItemConditionType(
            data.get("slot"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("slot", conditionType.slot)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final Optional<AttributeModifierSlot> slot;

    private final Comparison comparison;
    private final int compareTo;

    public PowerCountItemConditionType(Optional<AttributeModifierSlot> slot, Comparison comparison, int compareTo) {
        this.slot = slot;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(World world, ItemStack stack) {

        ItemPowersComponent itemPowers = stack.getOrDefault(ApoliDataComponentTypes.POWERS, ItemPowersComponent.DEFAULT);
        int powerCount = slot
            .map(itemPowers::matchingSlots)
            .orElseGet(itemPowers::size);

        return comparison.compare(powerCount, compareTo);

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return ItemConditionTypes.POWER_COUNT;
    }

}
