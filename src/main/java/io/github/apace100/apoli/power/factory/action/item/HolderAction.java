package io.github.apace100.apoli.power.factory.action.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.IdentifierAlias;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class HolderAction {

    public static void action(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {

        if(worldAndStack.getRight().isEmpty()) {
            return;
        }

        Entity holder = ((EntityLinkedItemStack) worldAndStack.getRight()).apoli$getEntity();
        if(holder == null) {
            return;
        }

        List<Consumer<Entity>> entityActions = new LinkedList<>();

        data.<Consumer<Entity>>ifPresent("entity_action", entityActions::add);
        data.<Consumer<Entity>>ifPresent("action", entityActions::add);

        entityActions.forEach(entityAction -> entityAction.accept(holder));

    }

    public static ActionFactory<Pair<World, ItemStack>> getFactory() {
        IdentifierAlias.addPathAlias("holder", "holder_action");
        return new ActionFactory<>(
            Apoli.identifier("holder_action"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("action", ApoliDataTypes.ENTITY_ACTION, null),
            HolderAction::action
        );
    }

}

