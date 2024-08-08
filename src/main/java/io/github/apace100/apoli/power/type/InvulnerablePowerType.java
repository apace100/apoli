package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import oshi.util.tuples.Pair;

import java.util.function.Predicate;

public class InvulnerablePowerType extends PowerType {

    private final Predicate<Pair<DamageSource, Float>> damageCondition;

    public InvulnerablePowerType(Power power, LivingEntity entity, Predicate<Pair<DamageSource, Float>> damageCondition) {
        super(power, entity);
        this.damageCondition = damageCondition;
    }

    public boolean doesApply(DamageSource source) {
        return damageCondition.test(new Pair<>(source, 0.0F));
    }

    public static PowerTypeFactory<InvulnerablePowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("invulnerability"),
            new SerializableData()
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION)
                .postProcessor(data -> {

                    //  TODO: Check if the damage condition is using the 'amount' type
                    //        and throw an exception if so.

                }),
            data -> (power, entity) -> new InvulnerablePowerType(power, entity,
                data.get("damage_condition")
            )
        ).allowCondition();
    }
}
