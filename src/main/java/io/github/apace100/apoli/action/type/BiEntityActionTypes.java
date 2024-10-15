package io.github.apace100.apoli.action.type;


import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.type.bientity.*;
import io.github.apace100.apoli.action.type.bientity.meta.*;
import io.github.apace100.apoli.action.type.meta.*;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.registry.Registry;

public class BiEntityActionTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();
    public static final SerializableDataType<ActionConfiguration<BiEntityActionType>> DATA_TYPE = SerializableDataType.registry(ApoliRegistries.BIENTITY_ACTION_TYPE, Apoli.MODID, ALIASES, (configurations, id) -> "Bi-entity action type \"" + id + "\" is undefined!");

    public static final ActionConfiguration<AndBiEntityActionType> AND = register(AndMetaActionType.createConfiguration(BiEntityAction.DATA_TYPE, AndBiEntityActionType::new));
    public static final ActionConfiguration<ChanceBiEntityActionType> CHANCE = register(ChanceMetaActionType.createConfiguration(BiEntityAction.DATA_TYPE, ChanceBiEntityActionType::new));
    public static final ActionConfiguration<ChoiceBiEntityActionType> CHOICE = register(ChoiceMetaActionType.createConfiguration(BiEntityAction.DATA_TYPE, ChoiceBiEntityActionType::new));
    public static final ActionConfiguration<DelayBiEntityActionType> DELAY = register(DelayMetaActionType.createConfiguration(BiEntityAction.DATA_TYPE, DelayBiEntityActionType::new));
    public static final ActionConfiguration<IfElseListBiEntityActionType> IF_ELSE_LIST = register(IfElseListMetaActionType.createConfiguration(BiEntityAction.DATA_TYPE, BiEntityCondition.DATA_TYPE, IfElseListBiEntityActionType::new));
    public static final ActionConfiguration<IfElseBiEntityActionType> IF_ELSE = register(IfElseMetaActionType.createConfiguration(BiEntityAction.DATA_TYPE, BiEntityCondition.DATA_TYPE, IfElseBiEntityActionType::new));
    public static final ActionConfiguration<NothingBiEntityActionType> NOTHING = register(NothingMetaActionType.createConfiguration(NothingBiEntityActionType::new));
    public static final ActionConfiguration<SideBiEntityActionType> SIDE = register(SideMetaActionType.createConfiguration(BiEntityAction.DATA_TYPE, SideBiEntityActionType::new));

    public static final ActionConfiguration<ActorActionBiEntityActionType> ACTOR_ACTION = register(ActionConfiguration.fromDataFactory(Apoli.identifier("actor_action"), ActorActionBiEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<InvertBiEntityActionType> INVERT = register(ActionConfiguration.fromDataFactory(Apoli.identifier("invert"), InvertBiEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<TargetActionBiEntityActionType> TARGET_ACTION = register(ActionConfiguration.fromDataFactory(Apoli.identifier("target_action"), TargetActionBiEntityActionType.DATA_FACTORY));

    public static final ActionConfiguration<AddToEntitySetBiEntityActionType> ADD_TO_ENTITY_SET = register(ActionConfiguration.fromDataFactory(Apoli.identifier("add_to_entity_set"), AddToEntitySetBiEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<AddVelocityBiEntityActionType> ADD_VELOCITY = register(ActionConfiguration.fromDataFactory(Apoli.identifier("add_velocity"), AddVelocityBiEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<DamageBiEntityActionType> DAMAGE = register(ActionConfiguration.fromDataFactory(Apoli.identifier("damage"), DamageBiEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<LeashBiEntityActionType> LEASH = register(ActionConfiguration.simple(Apoli.identifier("leash"), LeashBiEntityActionType::new));
    public static final ActionConfiguration<MountBiEntityActionType> MOUNT = register(ActionConfiguration.simple(Apoli.identifier("mount"), MountBiEntityActionType::new));
    public static final ActionConfiguration<RemoveFromEntitySetBiEntityActionType> REMOVE_FROM_ENTITY_SET = register(ActionConfiguration.fromDataFactory(Apoli.identifier("remove_from_entity_set"), RemoveFromEntitySetBiEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<TameBiEntityActionType> TAME = ActionConfiguration.simple(Apoli.identifier("tame"), TameBiEntityActionType::new);
    public static final ActionConfiguration<SetInLoveBiEntityActionType> SET_IN_LOVE = register(ActionConfiguration.simple(Apoli.identifier("set_in_love"), SetInLoveBiEntityActionType::new));

    public static void register() {

    }

    @SuppressWarnings("unchecked")
	public static <T extends BiEntityActionType> ActionConfiguration<T> register(ActionConfiguration<T> configuration) {

        ActionConfiguration<BiEntityActionType> casted = (ActionConfiguration<BiEntityActionType>) configuration;
        Registry.register(ApoliRegistries.BIENTITY_ACTION_TYPE, casted.id(), casted);

        return configuration;

    }

}
