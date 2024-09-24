package io.github.apace100.apoli.loot.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.component.item.ApoliDataComponentTypes;
import io.github.apace100.apoli.component.item.ItemPowersComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;

import java.util.EnumSet;
import java.util.List;

public class AddPowerLootFunction extends ConditionalLootFunction {

    public static final MapCodec<AddPowerLootFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> addConditionsField(instance).and(instance.group(
        SerializableDataTypes.ATTRIBUTE_MODIFIER_SLOT_SET.codec().optionalFieldOf("slot", EnumSet.of(AttributeModifierSlot.ANY)).forGetter(AddPowerLootFunction::slots),
        ApoliDataTypes.POWER_REFERENCE.codec().fieldOf("power").forGetter(AddPowerLootFunction::power),
        Codec.BOOL.optionalFieldOf("hidden", false).forGetter(AddPowerLootFunction::hidden),
        Codec.BOOL.optionalFieldOf("negative", false).forGetter(AddPowerLootFunction::negative)
    )).apply(instance, AddPowerLootFunction::new));

    private final EnumSet<AttributeModifierSlot> slots;
    private final PowerReference power;

    private final boolean hidden;
    private final boolean negative;

    private AddPowerLootFunction(List<LootCondition> conditions, EnumSet<AttributeModifierSlot> slots, PowerReference power, boolean hidden, boolean negative) {
        super(conditions);
        this.slots = slots;
        this.power = power;
        this.hidden = hidden;
        this.negative = negative;
    }

    @Override
    public LootFunctionType<? extends ConditionalLootFunction> getType() {
        return ApoliLootFunctionTypes.ADD_POWER;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {

        power().getOptionalReference().ifPresent(power -> {

            ItemPowersComponent itemPowers = stack.getOrDefault(ApoliDataComponentTypes.POWERS, ItemPowersComponent.DEFAULT);
            stack.set(ApoliDataComponentTypes.POWERS, ItemPowersComponent.builder(itemPowers)
                .add(slots(), power.getId(), hidden(), negative())
                .build());

        });

        return stack;

    }

    public EnumSet<AttributeModifierSlot> slots() {
        return slots;
    }

    public PowerReference power() {
        return power;
    }

    public boolean hidden() {
        return hidden;
    }

    public boolean negative() {
        return negative;
    }

}
