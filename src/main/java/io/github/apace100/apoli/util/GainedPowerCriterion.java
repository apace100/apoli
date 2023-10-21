package io.github.apace100.apoli.util;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.Optional;

public class GainedPowerCriterion extends AbstractCriterion<GainedPowerCriterion.Conditions> {

    public static final GainedPowerCriterion INSTANCE = new GainedPowerCriterion();
    public static final Identifier ID = Apoli.identifier("gained_power");

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, Optional<LootContextPredicate> predicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        Identifier id = new Identifier(JsonHelper.getString(obj, "power"));
        return new Conditions(predicate, id);
    }

    public void trigger(ServerPlayerEntity player, PowerType<?> type) {
        this.trigger(player, (conditions -> conditions.matches(type)));
    }

    public Identifier getId() {
        return ID;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class Conditions extends AbstractCriterionConditions {

        private final Identifier powerId;

        public Conditions(Optional<LootContextPredicate> predicate, Identifier powerId) {
            super(predicate);
            this.powerId = powerId;
        }

        public boolean matches(PowerType<?> powerType) {
            return powerType.getIdentifier().equals(powerId);
        }

        @Override
        public JsonObject toJson() {

            JsonObject jsonObject = super.toJson();
            jsonObject.addProperty("power", powerId.toString());

            return jsonObject;

        }

    }
}
