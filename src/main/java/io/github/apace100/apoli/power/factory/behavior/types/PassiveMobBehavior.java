package io.github.apace100.apoli.power.factory.behavior.types;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.behavior.MobBehaviorFactory;
import io.github.apace100.apoli.power.factory.behavior.MobBehavior;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class PassiveMobBehavior extends MobBehavior {
    public PassiveMobBehavior(MobEntity mob, Predicate<Pair<LivingEntity, LivingEntity>> bientityCondition) {
        super(mob, 0, bientityCondition);
    }

    @Override
    public boolean isPassive(LivingEntity target) {
        return doesApply(target);
    }

    public static MobBehaviorFactory<?> createFactory() {
        return new MobBehaviorFactory<>(Apoli.identifier("passive"),
                new SerializableData()
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
                (data, mob) -> new PassiveMobBehavior(mob, data.get("bientity_condition")));
    }
}