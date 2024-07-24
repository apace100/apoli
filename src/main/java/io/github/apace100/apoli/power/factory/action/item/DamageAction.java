package io.github.apace100.apoli.power.factory.action.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class DamageAction {

    public static void action(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {

        if (!(worldAndStack.getLeft() instanceof ServerWorld serverWorld)) {
            return;
        }

        ItemStack stack = worldAndStack.getRight();
        int damageAmount = data.getInt("amount");

        if (data.getBoolean("ignore_unbreaking")) {

            if (damageAmount >= stack.getMaxDamage()) {
                stack.decrement(1);
            }

            else {
                stack.setDamage(stack.getDamage() + damageAmount);
            }

        }

        else {
            stack.damage(damageAmount, serverWorld, null, item -> {});
        }

    }

    public static ActionFactory<Pair<World, StackReference>> getFactory() {
        return ItemActionFactory.createItemStackBased(
            Apoli.identifier("damage"),
            new SerializableData()
                .add("amount", SerializableDataTypes.INT, 1)
                .add("ignore_unbreaking", SerializableDataTypes.BOOLEAN, false),
            DamageAction::action
        );
    }

}
