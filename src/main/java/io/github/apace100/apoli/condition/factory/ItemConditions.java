package io.github.apace100.apoli.condition.factory;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.type.item.*;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class ItemConditions {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();

    public static void register() {
        MetaConditions.register(ApoliDataTypes.ITEM_CONDITION, ItemConditions::register);
        register(createSimpleFactory(Apoli.identifier("food"), FoodConditionType::condition));
        register(SmeltableConditionType.getFactory());
        register(IngredientConditionType.getFactory());
        register(ArmorValueConditionType.getFactory());
        register(EnchantmentConditionType.getFactory());
        register(CustomDataConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("fire_resistant"), stack -> stack.contains(DataComponentTypes.FIRE_RESISTANT)));
        register(createSimpleFactory(Apoli.identifier("enchantable"), ItemStack::isEnchantable));
        register(PowerCountConditionType.getFactory());
        register(HasPowerConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("empty"), ItemStack::isEmpty));
        register(AmountConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("damageable"), ItemStack::isDamageable));
        register(DurabilityConditionType.getFactory());
        register(RelativeDurabilityConditionType.getFactory());
        register(EquippableConditionType.getFactory());
        register(FuelConditionType.getFactory());
        register(ItemCooldownConditionType.getFactory());
        register(RelativeItemCooldownConditionType.getFactory());
    }

    public static ConditionTypeFactory<Pair<World, ItemStack>> createSimpleFactory(Identifier id, Predicate<ItemStack> predicate) {
        return new ConditionTypeFactory<>(id, new SerializableData(), (data, worldAndStack) -> predicate.test(worldAndStack.getRight()));
    }

    public static <F extends ConditionTypeFactory<Pair<World, ItemStack>>> F register(F conditionFactory) {
        return Registry.register(ApoliRegistries.ITEM_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }

}
