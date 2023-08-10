package io.github.apace100.apoli.power.factory.action.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class HolderAction {
    public static void action(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {
        if(worldAndStack.getRight().isEmpty()) {
            return;
        }
        Entity holder = ((EntityLinkedItemStack)worldAndStack.getRight()).getEntity();
        if(holder == null) {
            return;
        }
        Consumer<Entity> entityAction = data.get("entity_action");
        entityAction.accept(holder);
    }

    public static ActionFactory<Pair<World, ItemStack>> getFactory() {
        return new ActionFactory<>(Apoli.identifier("holder_action"),
            new SerializableData()
                .add("action", ApoliDataTypes.ENTITY_ACTION),
            HolderAction::action
        );
    }
}

