package io.github.apace100.apoli.power.factory.action;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.item.*;
import io.github.apace100.apoli.power.factory.action.meta.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.inventory.StackReference;
import net.minecraft.registry.Registry;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class ItemActions {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();

    public static void register() {
        register(AndAction.getFactory(ApoliDataTypes.ITEM_ACTIONS));
        register(ChanceAction.getFactory(ApoliDataTypes.ITEM_ACTION));
        register(IfElseAction.getFactory(ApoliDataTypes.ITEM_ACTION, ApoliDataTypes.ITEM_CONDITION,
                worldItemStackPair -> new Pair<>(worldItemStackPair.getLeft(), worldItemStackPair.getRight().get())));
        register(ChoiceAction.getFactory(ApoliDataTypes.ITEM_ACTION));
        register(IfElseListAction.getFactory(ApoliDataTypes.ITEM_ACTION, ApoliDataTypes.ITEM_CONDITION,
            worldItemStackPair -> new Pair<>(worldItemStackPair.getLeft(), worldItemStackPair.getRight().get())));
        register(DelayAction.getFactory(ApoliDataTypes.ITEM_ACTION));
        register(NothingAction.getFactory());
        register(SideAction.getFactory(ApoliDataTypes.ITEM_ACTION, worldAndStack -> !worldAndStack.getLeft().isClient));

        register(ItemActionFactory.createItemStackBased(Apoli.identifier("consume"), new SerializableData()
            .add("amount", SerializableDataTypes.INT, 1),
            (data, worldAndStack) -> worldAndStack.getRight().decrement(data.getInt("amount"))));
        register(ModifyAction.getFactory());
        register(DamageAction.getFactory());
        register(MergeCustomDataAction.getFactory());
        register(RemoveEnchantmentAction.getFactory());
        register(HolderAction.getFactory());
        register(ModifyItemCooldownAction.getFactory());
    }

    private static void register(ActionFactory<Pair<World, StackReference>> actionFactory) {
        Registry.register(ApoliRegistries.ITEM_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
