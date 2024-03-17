package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.bientity.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;
import net.minecraft.util.Pair;

public class BiEntityConditions {

    public static void register() {
        MetaConditions.register(ApoliDataTypes.BIENTITY_CONDITION, BiEntityConditions::register);
        register(InvertCondition.getFactory());
        register(ActorCondition.getFactory());
        register(TargetCondition.getFactory());
        register(EitherCondition.getFactory());
        register(BothCondition.getFactory());
        register(UndirectedCondition.getFactory());

        register(DistanceCondition.getFactory());
        register(CanSeeCondition.getFactory());
        register(OwnerCondition.getFactory());
        register(RidingCondition.getFactory());
        register(RidingRootCondition.getFactory());
        register(RidingRecursiveCondition.getFactory());
        register(AttackTargetCondition.getFactory());
        register(AttackerCondition.getFactory());
        register(RelativeRotationCondition.getFactory());
        register(EqualCondition.getFactory());
        register(InSetCondition.getFactory());
    }

    private static void register(ConditionFactory<Pair<Entity, Entity>> conditionFactory) {
        Registry.register(ApoliRegistries.BIENTITY_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
