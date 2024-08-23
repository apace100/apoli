package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class CustomDataConditionType {

    public static boolean condition(ItemStack stack, NbtCompound nbt) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).matches(nbt);
    }

    public static ConditionTypeFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("custom_data"),
            new SerializableData()
                .add("nbt", SerializableDataTypes.NBT_COMPOUND),
            (data, worldAndStack) -> condition(worldAndStack.getRight(),
                data.get("nbt")
            )
        );
    }

}
