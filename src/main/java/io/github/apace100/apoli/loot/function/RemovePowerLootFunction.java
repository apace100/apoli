package io.github.apace100.apoli.loot.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.component.item.ApoliDataComponentTypes;
import io.github.apace100.apoli.component.item.ItemPowersComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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

import java.util.*;

public class RemovePowerLootFunction extends ConditionalLootFunction {

    public static final MapCodec<RemovePowerLootFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> addConditionsField(instance).and(instance.group(
        SerializableDataTypes.ATTRIBUTE_MODIFIER_SLOT_SET.codec().optionalFieldOf("slot", EnumSet.allOf(AttributeModifierSlot.class)).forGetter(RemovePowerLootFunction::slots),
        Identifier.CODEC.fieldOf("power").forGetter(RemovePowerLootFunction::powerId)
    )).apply(instance, RemovePowerLootFunction::new));

    private final EnumSet<AttributeModifierSlot> slots;
    private final Identifier powerId;

    private RemovePowerLootFunction(List<LootCondition> conditions, EnumSet<AttributeModifierSlot> slots, Identifier powerId) {
        super(conditions);
        this.slots = slots;
        this.powerId = powerId;
    }

    @Override
    public LootFunctionType<? extends ConditionalLootFunction> getType() {
        return ApoliLootFunctionTypes.REMOVE_POWER;
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
        Power power = PowerManager.getOptional(powerId).orElse(null);

        if (entity == null || power == null) {
            return;
        }

        PowerHolderComponent powerComponent = PowerHolderComponent.KEY.getNullable(entity);
        if (powerComponent == null) {
            return;
        }

        Map<Identifier, Collection<Power>> revokedPowers = new HashMap<>();
        for (ItemPowersComponent.Entry entry : removedEntries) {

            AttributeModifierSlot modifierSlot = entry.slot();

            for (EquipmentSlot slot : EquipmentSlot.values()) {

                Identifier sourceId = Apoli.identifier("item/" + slot.getName());

                if (!revokedPowers.containsKey(sourceId) && modifierSlot.matches(slot)) {
                    revokedPowers
                        .computeIfAbsent(sourceId, k -> new ObjectArrayList<>())
                        .add(power);
                }

            }

        }

        if (!revokedPowers.isEmpty()) {
            PowerHolderComponent.revokePowers(entity, revokedPowers, true);
        }

    }

    public EnumSet<AttributeModifierSlot> slots() {
        return slots;
    }

    public Identifier powerId() {
        return powerId;
    }

}
