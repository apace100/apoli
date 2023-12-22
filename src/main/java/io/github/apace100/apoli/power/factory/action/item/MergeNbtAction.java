package io.github.apace100.apoli.power.factory.action.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class MergeNbtAction {

    public static void action(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {
        worldAndStack.getRight().getOrCreateNbt().copyFrom(data.get("nbt"));
    }

    public static ActionFactory<Pair<World, StackReference>> getFactory() {
        return ItemActionFactory.createItemStackBased(
            Apoli.identifier("merge_nbt"),
            new SerializableData()
                .add("nbt", SerializableDataTypes.NBT),
            MergeNbtAction::action
        );
    }

}
