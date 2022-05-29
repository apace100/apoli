package io.github.apace100.apoli.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

import java.util.Optional;

public class PowerLootCondition implements LootCondition {

    public static final LootConditionType TYPE = new LootConditionType(new PowerLootCondition.Serializer());

    private final Identifier powerId;
    private final Identifier powerSourceId;

    private PowerLootCondition(Identifier powerId) {
        this.powerId = powerId;
        this.powerSourceId = null;
    }

    private PowerLootCondition(Identifier powerId, Identifier powerSourceId) {
        this.powerId = powerId;
        this.powerSourceId = powerSourceId;
    }

    public LootConditionType getType() {
        return TYPE;
    }

    public boolean test(LootContext lootContext) {

        Optional<PowerHolderComponent> optionalPowerHolderComponent = PowerHolderComponent.KEY.maybeGet(
            lootContext.get(LootContextParameters.THIS_ENTITY)
        );

        if (optionalPowerHolderComponent.isPresent()) {

            PowerHolderComponent powerHolderComponent = optionalPowerHolderComponent.get();
            PowerType<?> powerType = PowerTypeRegistry.get(powerId);

            if (powerSourceId != null) return powerHolderComponent.hasPower(powerType, powerSourceId);
            else return powerHolderComponent.hasPower(powerType);

        }

        return false;

    }

    public static class Serializer implements JsonSerializer<PowerLootCondition> {

        public void toJson(JsonObject jsonObject, PowerLootCondition powerLootCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("power", powerLootCondition.powerId.toString());
            if (powerLootCondition.powerSourceId != null) jsonObject.addProperty("source", powerLootCondition.powerSourceId.toString());
        }

        public PowerLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            Identifier power = new Identifier(JsonHelper.getString(jsonObject, "power"));
            if (jsonObject.has("source")) {
                Identifier source = new Identifier(JsonHelper.getString(jsonObject, "source"));
                return new PowerLootCondition(power, source);
            }
            return new PowerLootCondition(power);
        }

    }

}
