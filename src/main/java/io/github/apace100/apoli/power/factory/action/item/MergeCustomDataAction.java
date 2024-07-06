package io.github.apace100.apoli.power.factory.action.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.action.ItemActions;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class MergeCustomDataAction {

    public static void action(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {

        ItemStack stack = worldAndStack.getRight();
        NbtCompound newNbt = data.get("nbt");

        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, oldNbt -> oldNbt.copyFrom(newNbt));

    }

    public static ActionFactory<Pair<World, StackReference>> getFactory() {

        var factory = ItemActionFactory.createItemStackBased(
            Apoli.identifier("merge_custom_data"),
            new SerializableData()
                .add("nbt", SerializableDataTypes.NBT),
            MergeCustomDataAction::action
        );

        ItemActions.ALIASES.addPathAlias("merge_nbt", factory.getSerializerId().getPath());
        return factory;

    }

}
