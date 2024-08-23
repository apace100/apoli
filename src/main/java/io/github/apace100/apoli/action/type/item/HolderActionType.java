package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.action.factory.ItemActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class HolderActionType {

    public static void action(ItemStack stack, Consumer<Entity> action) {

        Entity holder = ((EntityLinkedItemStack) stack).apoli$getEntity(true);

        if (holder != null) {
            action.accept(holder);
        }

    }

    public static ActionTypeFactory<Pair<World, StackReference>> getFactory() {
        return ItemActionTypeFactory.createItemStackBased(
            Apoli.identifier("holder_action"),
            new SerializableData()
                .add("action", ApoliDataTypes.ENTITY_ACTION),
            (data, worldAndStack) -> action(worldAndStack.getRight(),
                data.get("action")
            )
        );
    }

}

