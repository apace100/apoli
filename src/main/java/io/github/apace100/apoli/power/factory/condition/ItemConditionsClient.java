package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.power.factory.condition.item.SmeltableCondition;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;

@Environment(EnvType.CLIENT)
public class ItemConditionsClient {

    public static void register() {
        register(SmeltableCondition.getFactory(() -> MinecraftClient.getInstance().world));
    }

    private static void register(ConditionFactory<ItemStack> conditionFactory) {
        Registry.register(ApoliRegistries.ITEM_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
