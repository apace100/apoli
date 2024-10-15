package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.action.type.item.*;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.inventory.StackReference;
import net.minecraft.registry.Registry;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class ItemActionTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();
    public static final SerializableDataType<ActionConfiguration<ItemActionType>> DATA_TYPE = SerializableDataType.registry(ApoliRegistries.ITEM_ACTION_TYPE, Apoli.MODID, ALIASES, (configurations, id) -> "Item action type \"" + id + "\" is undefined!");

    public static void register() {

        MetaActionTypes.register(ApoliDataTypes.ITEM_ACTION, ApoliDataTypes.ITEM_CONDITION, worldAndStackRef -> new Pair<>(worldAndStackRef.getLeft(), worldAndStackRef.getRight().get()), ItemActionTypes::register);

        register(ConsumeActionType.getFactory());
        register(ModifyActionType.getFactory());
        register(DamageActionType.getFactory());
        register(MergeCustomDataActionType.getFactory());
        register(RemoveEnchantmentActionType.getFactory());
        register(HolderActionType.getFactory());
        register(ModifyItemCooldownActionType.getFactory());

    }

    public static <F extends ActionTypeFactory<Pair<World, StackReference>>> F register(F actionFactory) {
        return Registry.register(ApoliRegistries.ITEM_ACTION, actionFactory.getSerializerId(), actionFactory);
    }

}
