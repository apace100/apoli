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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class RelativeItemCooldownConditionType {

    public static boolean condition(ItemStack stack, Comparison comparison, float compareTo) {

        if (stack.isEmpty() || !(((EntityLinkedItemStack) stack).apoli$getEntity() instanceof PlayerEntity player)) {
            return false;
        }

        Item item = stack.getItem();
        ItemCooldownManager cooldownManager = player.getItemCooldownManager();

        return comparison.compare(cooldownManager.getCooldownProgress(item, 0.0F), compareTo);

    }

    public static ConditionTypeFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("relative_item_cooldown"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.FLOAT),
            (data, worldAndStack) -> condition(worldAndStack.getRight(),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
