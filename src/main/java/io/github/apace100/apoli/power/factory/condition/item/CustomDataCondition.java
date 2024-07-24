package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.power.factory.condition.ItemConditions;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class CustomDataCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {
        return worldAndStack.getRight().getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).matches(data.get("nbt"));
    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {

        var factory = new ConditionFactory<>(
            Apoli.identifier("custom_data"),
            new SerializableData()
                .add("nbt", SerializableDataTypes.NBT),
            CustomDataCondition::condition
        );

        ItemConditions.ALIASES.addPathAlias("nbt", factory.getSerializerId().getPath());
        return factory;

    }

}
