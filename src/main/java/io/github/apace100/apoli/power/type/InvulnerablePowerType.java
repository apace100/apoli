package io.github.apace100.apoli.power.type;

import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

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

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("invulnerability"),
            new SerializableData()
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION)
                .validate(data -> {

                    ConditionTypeFactory<Pair<DamageSource, Float>>.Instance damageCondition = data.get("damage_condition");

                    if (damageCondition.getFactory() == DamageConditionTypes.AMOUNT) {
                        return DataResult.error(() -> "Using the 'amount' damage condition type in a power that uses the 'invulnerability' power type is not allowed!");
                    }

                    else {
                        return DataResult.success(data);
                    }

                }),
            data -> (power, entity) -> new InvulnerablePowerType(power, entity,
                data.get("damage_condition")
            )
        ).allowCondition();
    }
}
