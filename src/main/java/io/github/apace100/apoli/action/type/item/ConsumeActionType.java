package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ItemActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.item.ItemStack;

public class ConsumeActionType {

    public static void action(ItemStack stack, int amount) {
        stack.decrement(amount);
    }

    public static ItemActionTypeFactory getFactory() {
        return ItemActionTypeFactory.createItemStackBased(
            Apoli.identifier("consume"),
            new SerializableData()
                .add("amount", SerializableDataTypes.INT, 1),
            (data, worldAndStack) -> action(worldAndStack.getRight(),
                data.get("amount")
            )
        );
    }

}
