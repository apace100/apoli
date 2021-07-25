package io.github.apace100.apoli.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class AddPowerLootFunction extends ConditionalLootFunction {

    public static final LootFunctionType TYPE = new LootFunctionType(new AddPowerLootFunction.Serializer());

    private final EquipmentSlot slot;
    private final Identifier powerId;
    private final boolean hidden;
    private final boolean negative;

    private AddPowerLootFunction(LootCondition[] conditions, EquipmentSlot slot, Identifier powerId, boolean hidden, boolean negative) {
        super(conditions);
        this.slot = slot;
        this.powerId = powerId;
        this.hidden = hidden;
        this.negative = negative;
    }

    public LootFunctionType getType() {
        return TYPE;
    }

    public ItemStack process(ItemStack stack, LootContext context) {
        StackPowerUtil.addPower(stack, slot, powerId, hidden, negative);
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(EquipmentSlot slot, Identifier powerId, boolean hidden, boolean negative) {
        return builder((conditions) -> new AddPowerLootFunction(conditions, slot, powerId, hidden, negative));
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<AddPowerLootFunction> {
        public void toJson(JsonObject jsonObject, AddPowerLootFunction addPowerLootFunction, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, addPowerLootFunction, jsonSerializationContext);
            jsonObject.addProperty("slot", addPowerLootFunction.slot.getName());
            jsonObject.addProperty("power", addPowerLootFunction.powerId.toString());
        }

        public AddPowerLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
            EquipmentSlot slot = SerializableDataTypes.EQUIPMENT_SLOT.read(jsonObject.get("slot"));
            Identifier powerId = SerializableDataTypes.IDENTIFIER.read(jsonObject.get("power"));
            boolean hidden = JsonHelper.getBoolean(jsonObject, "hidden", false);
            boolean negative = JsonHelper.getBoolean(jsonObject, "negative", false);
            return new AddPowerLootFunction(lootConditions, slot, powerId, hidden, negative);
        }
    }
}
