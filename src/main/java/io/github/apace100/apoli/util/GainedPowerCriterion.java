package io.github.apace100.apoli.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

public class GainedPowerCriterion extends AbstractCriterion<GainedPowerCriterion.Conditions> {

    public static final GainedPowerCriterion INSTANCE = new GainedPowerCriterion();
    public static final Identifier ID = Apoli.identifier("gained_power");

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, PowerType<?> powerType) {
        this.trigger(player, conditions -> conditions.matches(powerType));
    }

    public record Conditions(Optional<LootContextPredicate> player, Identifier powerId) implements AbstractCriterion.Conditions {

        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.createStrictOptionalFieldCodec(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, "player").forGetter(Conditions::player),
            Identifier.CODEC.fieldOf("power").forGetter(Conditions::powerId)
        ).apply(instance, Conditions::new));

        @Override
        public Optional<LootContextPredicate> player() {
            return player;
        }

        public boolean matches(PowerType<?> powerType) {
            return powerType.getIdentifier().equals(powerId);
        }

    }

}
