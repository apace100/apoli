package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.item.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class ItemConditions {

    public static void register() {
        MetaConditions.register(ApoliDataTypes.ITEM_CONDITION, ItemConditions::register);
        register(FoodCondition.getFactory());
        register(SmeltableCondition.getFactory());
        register(IngredientCondition.getFactory());
        register(ArmorValueCondition.getFactory());
        register(HarvestLevelCondition.getFactory());
        register(EnchantmentCondition.getFactory());
        register(MeatCondition.getFactory());
        register(NbtCondition.getFactory());
        register(FireproofCondition.getFactory());
        register(EnchantableCondition.getFactory());
        register(PowerCountCondition.getFactory());
        register(HasPowerCondition.getFactory());
        register(EmptyCondition.getFactory());
        register(AmountCondition.getFactory());
        register(DamageableCondition.getFactory());
        register(DurabilityCondition.getFactory());
        register(RelativeDurabilityCondition.getFactory());
        register(EquippableCondition.getFactory());
        register(FuelCondition.getFactory());
    }

    private static void register(ConditionFactory<Pair<World, ItemStack>> conditionFactory) {
        Registry.register(ApoliRegistries.ITEM_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }

}
