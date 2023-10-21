package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class NbtCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {
        return NbtHelper.matches(data.get("nbt"), worldAndStack.getRight().getNbt(), true);
    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("nbt"),
            new SerializableData()
                .add("nbt", SerializableDataTypes.NBT),
            NbtCondition::condition
        );
    }

}
