package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.action.factory.ItemActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class DamageActionType {

    public static void action(World world, ItemStack stack, int amount, boolean ignoreUnbreaking) {

        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        if (ignoreUnbreaking) {

            if (amount >= stack.getMaxDamage()) {
                stack.decrement(1);
            }

            else {
                stack.setDamage(stack.getDamage() + amount);
            }

        }

        else {
            stack.damage(amount, serverWorld, null, item -> {});
        }

    }

    public static ActionTypeFactory<Pair<World, StackReference>> getFactory() {
        return ItemActionTypeFactory.createItemStackBased(
            Apoli.identifier("damage"),
            new SerializableData()
                .add("amount", SerializableDataTypes.INT, 1)
                .add("ignore_unbreaking", SerializableDataTypes.BOOLEAN, false),
            (data, worldAndStack) -> action(worldAndStack.getLeft(), worldAndStack.getRight(),
                data.get("amount"),
                data.get("ignore_unbreaking")
            )
        );
    }

}
