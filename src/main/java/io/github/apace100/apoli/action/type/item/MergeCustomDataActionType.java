package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.action.factory.ItemActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class MergeCustomDataActionType {

    public static void action(ItemStack stack, NbtCompound nbt) {
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, oldNbt -> oldNbt.copyFrom(nbt));
    }

    public static ActionTypeFactory<Pair<World, StackReference>> getFactory() {
        return ItemActionTypeFactory.createItemStackBased(
            Apoli.identifier("merge_custom_data"),
            new SerializableData()
                .add("nbt", SerializableDataTypes.NBT),
            (data, worldAndStack) -> action(worldAndStack.getRight(),
                data.get("nbt")
            )
        );
    }

}
