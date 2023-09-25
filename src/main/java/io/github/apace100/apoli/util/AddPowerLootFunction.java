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
import net.minecraft.util.JsonHelper;

import java.util.EnumSet;

public class AddPowerLootFunction extends ConditionalLootFunction {

    public static final LootFunctionType TYPE = new LootFunctionType(new AddPowerLootFunction.Serializer());

    private final EnumSet<EquipmentSlot> slots;
    private final EquipmentSlot slot;
    private final Identifier powerId;
    private final boolean hidden;
    private final boolean negative;

    private AddPowerLootFunction(LootCondition[] conditions, EnumSet<EquipmentSlot> slots, EquipmentSlot slot, Identifier powerId, boolean hidden, boolean negative) {
        super(conditions);
        this.slots = slots;
        this.slot = slot;
        this.powerId = powerId;
        this.hidden = hidden;
        this.negative = negative;
    }

    private AddPowerLootFunction(LootCondition[] conditions, EquipmentSlot slot, Identifier powerId, boolean hidden, boolean negative) {
        this(conditions, null, slot, powerId, hidden, negative);
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

        StackPowerUtil.addPower(stack, slots, powerId, hidden, negative);
        return stack;

    }

    public static ConditionalLootFunction.Builder<?> builder(EquipmentSlot slot, Identifier powerId, boolean hidden, boolean negative) {
        return builder((conditions) -> new AddPowerLootFunction(conditions, slot, powerId, hidden, negative));
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<AddPowerLootFunction> {

        public void toJson(JsonObject jsonObject, AddPowerLootFunction addPowerLootFunction, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, addPowerLootFunction, jsonSerializationContext);
            jsonObject.addProperty("power", addPowerLootFunction.powerId.toString());
            if (addPowerLootFunction.slots != null) {

                JsonArray slotsJsonArray = new JsonArray();
                for (EquipmentSlot slot : addPowerLootFunction.slots) {
                    slotsJsonArray.add(slot.getName());
                }

                jsonObject.add("slots", slotsJsonArray);

            }

            if (addPowerLootFunction.slot != null) {
                jsonObject.addProperty("slot", addPowerLootFunction.slot.getName());
            }

        }

        public AddPowerLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {

            Identifier powerId = SerializableDataTypes.IDENTIFIER.read(jsonObject.get("power"));

            boolean hidden = JsonHelper.getBoolean(jsonObject, "hidden", false);
            boolean negative = JsonHelper.getBoolean(jsonObject, "negative", false);

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

            return new AddPowerLootFunction(lootConditions, slots, slot, powerId, hidden, negative);

        }

    }

}
