package io.github.apace100.apoli.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.component.item.ApoliDataComponentTypes;
import io.github.apace100.apoli.component.item.ItemPowersComponent;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.Identifier;

import java.util.EnumSet;
import java.util.List;

public class AddPowerLootFunction extends ConditionalLootFunction {

    public static final MapCodec<AddPowerLootFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> addConditionsField(instance).and(instance.group(
        SerializableDataTypes.ATTRIBUTE_MODIFIER_SLOT_SET.optionalFieldOf("slot", EnumSet.of(AttributeModifierSlot.ANY)).forGetter(AddPowerLootFunction::slots),
        Identifier.CODEC.fieldOf("power").forGetter(AddPowerLootFunction::powerId),
        Codec.BOOL.optionalFieldOf("hidden", false).forGetter(AddPowerLootFunction::hidden),
        Codec.BOOL.optionalFieldOf("negative", false).forGetter(AddPowerLootFunction::negative)
    )).apply(instance, AddPowerLootFunction::new));
    public static final LootFunctionType<AddPowerLootFunction> TYPE = new LootFunctionType<>(MAP_CODEC);

    private final EnumSet<AttributeModifierSlot> slots;
    private final Identifier powerId;

    private final boolean hidden;
    private final boolean negative;

    private AddPowerLootFunction(List<LootCondition> conditions, EnumSet<AttributeModifierSlot> slots, Identifier powerId, boolean hidden, boolean negative) {
        super(conditions);
        this.slots = slots;
        this.powerId = powerId;
        this.hidden = hidden;
        this.negative = negative;
    }

    @Override
    public LootFunctionType<? extends ConditionalLootFunction> getType() {
        return TYPE;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {

        ItemPowersComponent itemPowers = stack.getOrDefault(ApoliDataComponentTypes.POWERS, ItemPowersComponent.DEFAULT);
        stack.set(ApoliDataComponentTypes.POWERS, ItemPowersComponent.builder(itemPowers)
            .add(slots, powerId, hidden, negative)
            .build());

        return stack;

    }

    public EnumSet<AttributeModifierSlot> slots() {
        return slots;
    }

    public Identifier powerId() {
        return powerId;
    }

    public boolean hidden() {
        return hidden;
    }

    public boolean negative() {
        return negative;
    }

}
