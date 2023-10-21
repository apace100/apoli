package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.registry.ApoliRegistries;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class ItemConditionsClient {

    public static void register() {
    }

    private static void register(ConditionFactory<Pair<World, ItemStack>> conditionFactory) {
        Registry.register(ApoliRegistries.ITEM_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }

}
