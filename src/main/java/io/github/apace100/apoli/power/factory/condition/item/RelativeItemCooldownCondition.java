package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class RelativeItemCooldownCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {

        ItemStack stack = worldAndStack.getRight();
        Entity stackHolder = ((EntityLinkedItemStack) stack).apoli$getEntity();

        if (stack.isEmpty() || !(stackHolder instanceof PlayerEntity player)) {
            return false;
        }

        Comparison comparison = data.get("comparison");
        float compareTo = data.get("compare_to");

        Item item = stack.getItem();
        ItemCooldownManager cooldownManager = player.getItemCooldownManager();

        return comparison.compare(cooldownManager.getCooldownProgress(item, 0.0F), compareTo);

    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("relative_item_cooldown"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.FLOAT),
            RelativeItemCooldownCondition::condition
        );
    }

}
