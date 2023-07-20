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

public class RemovePowerLootFunction extends ConditionalLootFunction {

    public static final LootFunctionType TYPE = new LootFunctionType(new io.github.apace100.apoli.util.RemovePowerLootFunction.Serializer());

    private final EquipmentSlot slot;
    private final Identifier powerId;

    private RemovePowerLootFunction(LootCondition[] conditions, EquipmentSlot slot, Identifier powerId) {
        super(conditions);
        this.slot = slot;
        this.powerId = powerId;
    }

    public LootFunctionType getType() {
        return TYPE;
    }

    public ItemStack process(ItemStack stack, LootContext context) {
        StackPowerUtil.removePower(stack, slot, powerId);
        return stack;
    }

    public static net.minecraft.loot.function.ConditionalLootFunction.Builder<?> builder(EquipmentSlot slot, Identifier powerId) {
        return builder((conditions) -> new RemovePowerLootFunction(conditions, slot, powerId));
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<RemovePowerLootFunction> {
        public void toJson(JsonObject jsonObject, RemovePowerLootFunction addPowerLootFunction, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, addPowerLootFunction, jsonSerializationContext);
            jsonObject.addProperty("slot", addPowerLootFunction.slot.getName());
            jsonObject.addProperty("power", addPowerLootFunction.powerId.toString());
        }

        public RemovePowerLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
            EquipmentSlot slot = SerializableDataTypes.EQUIPMENT_SLOT.read(jsonObject.get("slot"));
            Identifier powerId = SerializableDataTypes.IDENTIFIER.read(jsonObject.get("power"));
            return new RemovePowerLootFunction(lootConditions, slot, powerId);
        }
    }
}
