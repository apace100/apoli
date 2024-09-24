package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.condition.type.bientity.*;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.Objects;
import java.util.function.BiPredicate;

public class BiEntityConditionTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();

    public static void register() {
        MetaConditionTypes.register(ApoliDataTypes.BIENTITY_CONDITION, BiEntityConditionTypes::register);
        register(InvertConditionType.getFactory());
        register(ActorConditionType.getFactory());
        register(TargetConditionType.getFactory());
        register(EitherConditionType.getFactory());
        register(BothConditionType.getFactory());
        register(UndirectedConditionType.getFactory());

        register(DistanceConditionType.getFactory());
        register(CanSeeConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("owner"), OwnerConditionType::condition));
        register(createSimpleFactory(Apoli.identifier("riding"), RidingConditionType::condition));
        register(createSimpleFactory(Apoli.identifier("riding_root"), RidingRootConditionType::condition));
        register(createSimpleFactory(Apoli.identifier("riding_recursive"), RidingRecursiveConditionType::condition));
        register(createSimpleFactory(Apoli.identifier("attack_target"), AttackTargetConditionType::condition));
        register(createSimpleFactory(Apoli.identifier("attacker"), AttackerConditionType::condition));
        register(RelativeRotationConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("equal"), Objects::equals));
        register(InEntitySetConditionType.getFactory());
    }

    public static ConditionTypeFactory<Pair<Entity, Entity>> createSimpleFactory(Identifier id, BiPredicate<Entity, Entity> condition) {
        return new ConditionTypeFactory<>(id, new SerializableData(), (data, actorAndTarget) -> condition.test(actorAndTarget.getLeft(), actorAndTarget.getRight()));
    }

    public static <F extends ConditionTypeFactory<Pair<Entity, Entity>>> F register(F conditionFactory) {
        return Registry.register(ApoliRegistries.BIENTITY_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }

}
