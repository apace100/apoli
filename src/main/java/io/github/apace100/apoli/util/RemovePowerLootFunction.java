package io.github.apace100.apoli.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.component.item.ApoliDataComponentTypes;
import io.github.apace100.apoli.component.item.ItemPowersComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class RemovePowerLootFunction extends ConditionalLootFunction {

    public static final MapCodec<RemovePowerLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> addConditionsField(instance).and(instance.group(
        ApoliCodecs.ATTRIBUTE_MODIFIER_SLOT_SET.optionalFieldOf("slot", EnumSet.allOf(AttributeModifierSlot.class)).forGetter(RemovePowerLootFunction::slots),
        Identifier.CODEC.fieldOf("power").forGetter(RemovePowerLootFunction::powerId)
    )).apply(instance, RemovePowerLootFunction::new));
    public static final LootFunctionType<RemovePowerLootFunction> TYPE = new LootFunctionType<>(CODEC);

    private final EnumSet<AttributeModifierSlot> slots;
    private final Identifier powerId;

    private RemovePowerLootFunction(List<LootCondition> conditions, EnumSet<AttributeModifierSlot> slots, Identifier powerId) {
        super(conditions);
        this.slots = slots;
        this.powerId = powerId;
    }

    @Override
    public LootFunctionType<? extends ConditionalLootFunction> getType() {
        return TYPE;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {

        ItemPowersComponent itemPowers = stack.get(ApoliDataComponentTypes.POWERS);
        ComponentChanges.Builder componentChanges = ComponentChanges.builder();

        if (itemPowers == null) {
            return stack;
        }

        ItemPowersComponent newItemPowers = ItemPowersComponent.builder(itemPowers)
            .remove(slots, powerId, removedEntries -> onSlotsRemoval(context, removedEntries))
            .build();

        if (newItemPowers.isEmpty()) {
            componentChanges.remove(ApoliDataComponentTypes.POWERS);
        }

        else {
            componentChanges.add(ApoliDataComponentTypes.POWERS, newItemPowers);
        }

        stack.applyChanges(componentChanges.build());
        return stack;

    }

    protected void onSlotsRemoval(LootContext context, Collection<ItemPowersComponent.Entry> removedEntries) {

        Entity entity = context.get(LootContextParameters.THIS_ENTITY);
        PowerType<?> power = PowerTypeRegistry.getNullable(powerId);

        if (entity == null || power == null) {
            return;
        }

        PowerHolderComponent powerComponent = PowerHolderComponent.KEY.getNullable(entity);
        boolean shouldSync = false;

        if (powerComponent == null) {
            return;
        }

        EnumSet<EquipmentSlot> processedSlots = EnumSet.noneOf(EquipmentSlot.class);
        for (ItemPowersComponent.Entry entry : removedEntries) {

            AttributeModifierSlot modifierSlot = entry.slot();
            for (EquipmentSlot slot : EquipmentSlot.values()) {

                if (!modifierSlot.matches(slot) || processedSlots.contains(slot)) {
                    continue;
                }

                Identifier sourceId = Apoli.identifier("item/" + slot.getName());
                shouldSync |= powerComponent.removePower(power, sourceId);

                processedSlots.add(slot);

            }

        }

        if (shouldSync) {
            powerComponent.sync();
        }

    }

    public EnumSet<AttributeModifierSlot> slots() {
        return slots;
    }

    public Identifier powerId() {
        return powerId;
    }

}
