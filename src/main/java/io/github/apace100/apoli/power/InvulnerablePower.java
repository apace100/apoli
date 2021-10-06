package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class InvulnerablePower extends Power {

    private final Predicate<DamageSource> damageSources;

    public InvulnerablePower(PowerType<?> type, LivingEntity entity, Predicate<DamageSource> damageSourcePredicate) {
        super(type, entity);
        this.damageSources = damageSourcePredicate;
    }

    public boolean doesApply(DamageSource source) {
        return damageSources.test(source);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("invulnerability"),
            new SerializableData()
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION),
            data ->
                (type, player) -> {
                    ConditionFactory<Pair<DamageSource, Float>>.Instance damageCondition =
                        (ConditionFactory<Pair<DamageSource, Float>>.Instance)data.get("damage_condition");
                    return new InvulnerablePower(type, player, ds -> damageCondition.test(new Pair<>(ds, null)));
                })
            .allowCondition();
    }
}
