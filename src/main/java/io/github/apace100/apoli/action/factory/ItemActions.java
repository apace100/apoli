package io.github.apace100.apoli.action.factory;

import io.github.apace100.apoli.action.type.item.*;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.inventory.StackReference;
import net.minecraft.registry.Registry;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class ItemActions {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();

    public static void register() {

        MetaActions.register(ApoliDataTypes.ITEM_ACTION, ApoliDataTypes.ITEM_CONDITION, worldAndStackRef -> new Pair<>(worldAndStackRef.getLeft(), worldAndStackRef.getRight().get()), ItemActions::register);

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
