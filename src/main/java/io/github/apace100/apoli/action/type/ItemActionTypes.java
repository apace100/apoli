package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.type.item.*;
import io.github.apace100.apoli.action.type.item.meta.*;
import io.github.apace100.apoli.action.type.meta.*;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.registry.Registry;

public class ItemActionTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();
    public static final SerializableDataType<ActionConfiguration<ItemActionType>> DATA_TYPE = SerializableDataType.registry(ApoliRegistries.ITEM_ACTION_TYPE, Apoli.MODID, ALIASES, (configurations, id) -> "Item action type \"" + id + "\" is undefined!");

    public static final ActionConfiguration<AndItemActionType> AND = register(AndMetaActionType.createConfiguration(ItemAction.DATA_TYPE, AndItemActionType::new));
    public static final ActionConfiguration<ChanceItemActionType> CHANCE = register(ChanceMetaActionType.createConfiguration(ItemAction.DATA_TYPE, ChanceItemActionType::new));
    public static final ActionConfiguration<ChoiceItemActionType> CHOICE = register(ChoiceMetaActionType.createConfiguration(ItemAction.DATA_TYPE, ChoiceItemActionType::new));
    public static final ActionConfiguration<DelayItemActionType> DELAY = register(DelayMetaActionType.createConfiguration(ItemAction.DATA_TYPE, DelayItemActionType::new));
    public static final ActionConfiguration<IfElseListItemActionType> IF_ELSE_LIST = register(IfElseListMetaActionType.createConfiguration(ItemAction.DATA_TYPE, ItemCondition.DATA_TYPE, IfElseListItemActionType::new));
    public static final ActionConfiguration<IfElseItemActionType> IF_ELSE = register(IfElseMetaActionType.createConfiguration(ItemAction.DATA_TYPE, ItemCondition.DATA_TYPE, IfElseItemActionType::new));
    public static final ActionConfiguration<NothingItemActionType> NOTHING = register(NothingMetaActionType.createConfiguration(NothingItemActionType::new));
    public static final ActionConfiguration<SideItemActionType> SIDE = register(SideMetaActionType.createConfiguration(ItemAction.DATA_TYPE, SideItemActionType::new));

    public static final ActionConfiguration<ConsumeItemActionType> CONSUME = register(ActionConfiguration.of(Apoli.identifier("consume"), ConsumeItemActionType.DATA_FACTORY));
    public static final ActionConfiguration<DamageItemActionType> DAMAGE = register(ActionConfiguration.of(Apoli.identifier("damage"), DamageItemActionType.DATA_FACTORY));
    public static final ActionConfiguration<HolderActionItemActionType> HOLDER = register(ActionConfiguration.of(Apoli.identifier("holder_action"), HolderActionItemActionType.DATA_FACTORY));
    public static final ActionConfiguration<MergeCustomDataItemActionType> MERGE_CUSTOM_DATA = register(ActionConfiguration.of(Apoli.identifier("merge_custom_data"), MergeCustomDataItemActionType.DATA_FACTORY));
    public static final ActionConfiguration<ModifyItemActionType> MODIFY = register(ActionConfiguration.of(Apoli.identifier("modify"), ModifyItemActionType.DATA_FACTORY));
    public static final ActionConfiguration<ModifyItemCooldownItemActionType> MODIFY_ITEM_COOLDOWN = register(ActionConfiguration.of(Apoli.identifier("modify_item_cooldown"), ModifyItemCooldownItemActionType.DATA_FACTORY));
    public static final ActionConfiguration<RemoveEnchantmentItemActionType> REMOVE_ENCHANTMENT = register(ActionConfiguration.of(Apoli.identifier("remove_enchantment"), RemoveEnchantmentItemActionType.DATA_FACTORY));

    public static void register() {

    }

    @SuppressWarnings("unchecked")
	public static <T extends ItemActionType> ActionConfiguration<T> register(ActionConfiguration<T> configuration) {

        ActionConfiguration<ItemActionType> casted = (ActionConfiguration<ItemActionType>) configuration;
        Registry.register(ApoliRegistries.ITEM_ACTION_TYPE, casted.id(), casted);

        return configuration;

    }

}
