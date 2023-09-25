package io.github.apace100.apoli.util;

import com.google.gson.*;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.Identifier;

import java.util.EnumSet;

public class RemovePowerLootFunction extends ConditionalLootFunction {

    public static final LootFunctionType TYPE = new LootFunctionType(new io.github.apace100.apoli.util.RemovePowerLootFunction.Serializer());

    private final EnumSet<EquipmentSlot> slots;
    private final EquipmentSlot slot;
    private final Identifier powerId;

    private RemovePowerLootFunction(LootCondition[] conditions, EnumSet<EquipmentSlot> slots, EquipmentSlot slot, Identifier powerId) {
        super(conditions);
        this.slots = slots;
        this.slot = slot;
        this.powerId = powerId;
    }

    private RemovePowerLootFunction(LootCondition[] conditions, EquipmentSlot slot, Identifier powerId) {
        this(conditions, null, slot, powerId);
    }

    public LootFunctionType getType() {
        return TYPE;
    }

    public ItemStack process(ItemStack stack, LootContext context) {

        EnumSet<EquipmentSlot> slots = EnumSet.noneOf(EquipmentSlot.class);
        if (this.slots != null) {
            slots.addAll(this.slots);
        }

        if (this.slot != null) {
            slots.add(this.slot);
        }

        StackPowerUtil.removePower(stack, slots, powerId);
        return stack;

    }

    public static net.minecraft.loot.function.ConditionalLootFunction.Builder<?> builder(EquipmentSlot slot, Identifier powerId) {
        return builder((conditions) -> new RemovePowerLootFunction(conditions, slot, powerId));
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<RemovePowerLootFunction> {

        public void toJson(JsonObject jsonObject, RemovePowerLootFunction removePowerLootFunction, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, removePowerLootFunction, jsonSerializationContext);
            jsonObject.addProperty("power", removePowerLootFunction.powerId.toString());
            if (removePowerLootFunction.slots != null) {

                JsonArray slotsJsonArray = new JsonArray();
                for (EquipmentSlot slot : removePowerLootFunction.slots) {
                    slotsJsonArray.add(slot.getName());
                }

                jsonObject.add("slots", slotsJsonArray);

            }

            if (removePowerLootFunction.slot != null) {
                jsonObject.addProperty("slot", removePowerLootFunction.slot.getName());
            }

        }

        public RemovePowerLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {

            Identifier powerId = SerializableDataTypes.IDENTIFIER.read(jsonObject.get("power"));

            EnumSet<EquipmentSlot> slots = null;
            if (jsonObject.has("slots") && jsonObject.get("slots") instanceof JsonArray slotsJsonArray) {
                slots = EnumSet.noneOf(EquipmentSlot.class);
                for (JsonElement jsonElement : slotsJsonArray) {
                    slots.add(SerializableDataTypes.EQUIPMENT_SLOT.read(jsonElement));
                }
            }

            EquipmentSlot slot = null;
            if (jsonObject.has("slot")) {
                slot = SerializableDataTypes.EQUIPMENT_SLOT.read(jsonObject.get("slot"));
            }

            return new RemovePowerLootFunction(lootConditions, slots, slot, powerId);

        }

    }

}
