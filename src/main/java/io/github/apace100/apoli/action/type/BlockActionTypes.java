package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.type.block.*;
import io.github.apace100.apoli.action.type.block.meta.*;
import io.github.apace100.apoli.action.type.meta.*;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.registry.Registry;

public class BlockActionTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();
    public static final SerializableDataType<ActionConfiguration<BlockActionType>> DATA_TYPE = SerializableDataType.registry(ApoliRegistries.BLOCK_ACTION_TYPE, Apoli.MODID, ALIASES, (configurations, id) -> "Block action type \"" + id + "\" is undefined!");

	public static final ActionConfiguration<AndBlockActionType> AND = register(AndMetaActionType.createConfiguration(BlockAction.DATA_TYPE, AndBlockActionType::new));
    public static final ActionConfiguration<ChanceBlockActionType> CHANCE = register(ChanceMetaActionType.createConfiguration(BlockAction.DATA_TYPE, ChanceBlockActionType::new));
    public static final ActionConfiguration<ChoiceBlockActionType> CHOICE = register(ChoiceMetaActionType.createConfiguration(BlockAction.DATA_TYPE, ChoiceBlockActionType::new));
    public static final ActionConfiguration<DelayBlockActionType> DELAY = register(DelayMetaActionType.createConfiguration(BlockAction.DATA_TYPE, DelayBlockActionType::new));
    public static final ActionConfiguration<IfElseListBlockActionType> IF_ELSE_LIST = register(IfElseListMetaActionType.createConfiguration(BlockAction.DATA_TYPE, BlockCondition.DATA_TYPE, IfElseListBlockActionType::new));
    public static final ActionConfiguration<IfElseBlockActionType> IF_ELSE = register(IfElseMetaActionType.createConfiguration(BlockAction.DATA_TYPE, BlockCondition.DATA_TYPE, IfElseBlockActionType::new));
    public static final ActionConfiguration<NothingBlockActionType> NOTHING = register(NothingMetaActionType.createConfiguration(NothingBlockActionType::new));
    public static final ActionConfiguration<SideBlockActionType> SIDE = register(SideMetaActionType.createConfiguration(BlockAction.DATA_TYPE, SideBlockActionType::new));

    public static final ActionConfiguration<OffsetBlockActionType> OFFSET = register(ActionConfiguration.of(Apoli.identifier("offset"), OffsetBlockActionType.DATA_FACTORY));

    public static final ActionConfiguration<AddBlockBlockActionType> ADD_BLOCK = register(ActionConfiguration.of(Apoli.identifier("add_block"), AddBlockBlockActionType.DATA_FACTORY));
    public static final ActionConfiguration<AreaOfEffectBlockActionType> AREA_OF_EFFECT = register(ActionConfiguration.of(Apoli.identifier("area_of_effect"), AreaOfEffectBlockActionType.DATA_FACTORY));
    public static final ActionConfiguration<BoneMealBlockActionType> BONE_MEAL = register(ActionConfiguration.of(Apoli.identifier("bonemeal"), BoneMealBlockActionType.DATA_FACTORY));
    public static final ActionConfiguration<ExecuteCommandBlockActionType> EXECUTE_COMMAND = register(ActionConfiguration.of(Apoli.identifier("execute_command"), ExecuteCommandBlockActionType.DATA_FACTORY));
    public static final ActionConfiguration<ExplodeBlockActionType> EXPLODE = register(ActionConfiguration.of(Apoli.identifier("explode"), ExplodeBlockActionType.DATA_FACTORY));
    public static final ActionConfiguration<ModifyBlockStateBlockActionType> MODIFY_BLOCK_STATE = register(ActionConfiguration.of(Apoli.identifier("modify_block_state"), ModifyBlockStateBlockActionType.DATA_FACTORY));
    public static final ActionConfiguration<SetBlockBlockActionType> SET_BLOCK = register(ActionConfiguration.of(Apoli.identifier("set_block"), SetBlockBlockActionType.DATA_FACTORY));
    public static final ActionConfiguration<SpawnEntityBlockActionType> SPAWN_ENTITY = register(ActionConfiguration.of(Apoli.identifier("spawn_entity"), SpawnEntityBlockActionType.DATA_FACTORY));

    public static void register() {

    }

    @SuppressWarnings("unchecked")
	public static <T extends BlockActionType> ActionConfiguration<T> register(ActionConfiguration<T> configuration) {

        ActionConfiguration<BlockActionType> casted = (ActionConfiguration<BlockActionType>) configuration;
        Registry.register(ApoliRegistries.BLOCK_ACTION_TYPE, casted.id(), casted);

        return configuration;

    }

}
