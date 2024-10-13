package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.bientity.*;
import io.github.apace100.apoli.condition.type.bientity.meta.*;
import io.github.apace100.apoli.condition.type.meta.AllOfMetaConditionType;
import io.github.apace100.apoli.condition.type.meta.AnyOfMetaConditionType;
import io.github.apace100.apoli.condition.type.meta.ConstantMetaConditionType;
import io.github.apace100.apoli.condition.type.meta.RandomChanceMetaConditionType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.registry.Registry;

public class BiEntityConditionTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();
    public static final SerializableDataType<ConditionConfiguration<BiEntityConditionType>> DATA_TYPE = SerializableDataType.registry(ApoliRegistries.BIENTITY_CONDITION_TYPE, Apoli.MODID, ALIASES, (configurations, id) -> "Bi-entity condition type \"" + id + "\" is undefined!");

    public static final ConditionConfiguration<AllOfBiEntityConditionType> ALL_OF = register(AllOfMetaConditionType.createConfiguration(BiEntityCondition.DATA_TYPE, AllOfBiEntityConditionType::new));
    public static final ConditionConfiguration<AnyOfBiEntityConditionType> ANY_OF = register(AnyOfMetaConditionType.createConfiguration(BiEntityCondition.DATA_TYPE, AnyOfBiEntityConditionType::new));
    public static final ConditionConfiguration<ConstantBiEntityConditionType> CONSTANT = register(ConstantMetaConditionType.createConfiguration(ConstantBiEntityConditionType::new));
    public static final ConditionConfiguration<RandomChanceBiEntityConditionType> RANDOM_CHANCE = register(RandomChanceMetaConditionType.createConfiguration(RandomChanceBiEntityConditionType::new));

    public static final ConditionConfiguration<ActorConditionBiEntityConditionType> ACTOR_CONDITION = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("actor_condition"), ActorConditionBiEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<BothBiEntityConditionType> BOTH = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("both"), BothBiEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<EitherBiEntityConditionType> EITHER = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("either"), EitherBiEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<InvertBiEntityConditionType> INVERT = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("invert"), InvertBiEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<TargetConditionBiEntityConditionType> TARGET_CONDITION = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("target_condition"), TargetConditionBiEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<UndirectedBiEntityConditionType> UNDIRECTED = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("undirected"), UndirectedBiEntityConditionType.DATA_FACTORY));

    public static final ConditionConfiguration<AttackTargetBiEntityConditionType> ATTACK_TARGET = register(ConditionConfiguration.simple(Apoli.identifier("attack_target"), AttackTargetBiEntityConditionType::new));
    public static final ConditionConfiguration<AttackerBiEntityConditionType> ATTACKER = register(ConditionConfiguration.simple(Apoli.identifier("attacker"), AttackerBiEntityConditionType::new));
    public static final ConditionConfiguration<CanSeeBiEntityConditionType> CAN_SEE = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("can_see"), CanSeeBiEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<DistanceBiEntityConditionType> DISTANCE = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("distance"), DistanceBiEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<EqualBiEntityConditionType> EQUAL = ConditionConfiguration.simple(Apoli.identifier("equal"), EqualBiEntityConditionType::new);
    public static final ConditionConfiguration<InEntitySetBiEntityConditionType> IN_ENTITY_SET = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("in_entity_set"), InEntitySetBiEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<OwnerBiEntityConditionType> OWNER = register(ConditionConfiguration.simple(Apoli.identifier("owner"), OwnerBiEntityConditionType::new));
    public static final ConditionConfiguration<RelativeRotationBiEntityConditionType> RELATIVE_ROTATION = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("relative_rotation"), RelativeRotationBiEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<RidingBiEntityConditionType> RIDING = register(ConditionConfiguration.simple(Apoli.identifier("riding"), RidingBiEntityConditionType::new));
    public static final ConditionConfiguration<RidingRecursiveBiEntityConditionType> RIDING_RECURSIVE = register(ConditionConfiguration.simple(Apoli.identifier("riding_recursive"), RidingRecursiveBiEntityConditionType::new));
    public static final ConditionConfiguration<RidingRootBiEntityConditionType> RIDING_ROOT = register(ConditionConfiguration.simple(Apoli.identifier("riding_root"), RidingRootBiEntityConditionType::new));

    public static void register() {

    }

    @SuppressWarnings("unchecked")
    public static <CT extends BiEntityConditionType> ConditionConfiguration<CT> register(ConditionConfiguration<CT> configuration) {

        ConditionConfiguration<BiEntityConditionType> casted = (ConditionConfiguration<BiEntityConditionType>) configuration;
        Registry.register(ApoliRegistries.BIENTITY_CONDITION_TYPE, casted.id(), casted);

        return configuration;

    }

}
