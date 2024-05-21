package io.github.apace100.apoli.power.factory.action.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.action.ItemActions;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class HolderAction {

    public static void action(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {

        Entity holder = ((EntityLinkedItemStack) worldAndStack.getRight()).apoli$getEntity();
        Consumer<Entity> entityAction = data.get("action");

        if (holder != null && entityAction != null) {
            entityAction.accept(holder);
        }

    }

    public static ActionFactory<Pair<World, StackReference>> getFactory() {

        ActionFactory<Pair<World, StackReference>> factory = ItemActionFactory.createItemStackBased(
            Apoli.identifier("holder_action"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .addFunctionedDefault("action", ApoliDataTypes.ENTITY_ACTION, data -> data.get("entity_action")),
            HolderAction::action
        );

        ItemActions.ALIASES.addPathAlias("holder", factory.getSerializerId().getPath());
        return factory;

    }

}

