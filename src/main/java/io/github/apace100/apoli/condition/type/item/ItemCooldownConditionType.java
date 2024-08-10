package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class ItemCooldownConditionType {

    public static boolean condition(ItemStack stack, Comparison comparison, int compareTo) {

        if (stack.isEmpty() || !(((EntityLinkedItemStack) stack).apoli$getEntity() instanceof PlayerEntity player)) {
            return false;
        }

        ItemCooldownManager.Entry cooldownEntry = player.getItemCooldownManager().entries.get(stack.getItem());
        int cooldown = cooldownEntry != null
            ? cooldownEntry.endTick - cooldownEntry.startTick
            : 0;

        return comparison.compare(cooldown, compareTo);

    }

    public static ConditionTypeFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("item_cooldown"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            (data, worldAndStack) -> condition(worldAndStack.getRight(),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
