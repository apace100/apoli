package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.condition.type.item.*;
import io.github.apace100.apoli.condition.type.item.meta.AllOfItemConditionType;
import io.github.apace100.apoli.condition.type.item.meta.AnyOfItemConditionType;
import io.github.apace100.apoli.condition.type.item.meta.ConstantItemConditionType;
import io.github.apace100.apoli.condition.type.item.meta.RandomChanceItemConditionType;
import io.github.apace100.apoli.condition.type.meta.AllOfMetaConditionType;
import io.github.apace100.apoli.condition.type.meta.AnyOfMetaConditionType;
import io.github.apace100.apoli.condition.type.meta.ConstantMetaConditionType;
import io.github.apace100.apoli.condition.type.meta.RandomChanceMetaConditionType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.registry.Registry;

public class ItemConditionTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();
    public static final SerializableDataType<ConditionConfiguration<ItemConditionType>> DATA_TYPE = SerializableDataType.registry(ApoliRegistries.ITEM_CONDITION_TYPE, Apoli.MODID, ALIASES, (configurations, id) -> "Item condition type \"" + id + "\" is undefined!");

    public static final ConditionConfiguration<AllOfItemConditionType> ALL_OF = register(AllOfMetaConditionType.createConfiguration(ItemCondition.DATA_TYPE, AllOfItemConditionType::new));
    public static final ConditionConfiguration<AnyOfItemConditionType> ANY_OF = register(AnyOfMetaConditionType.createConfiguration(ItemCondition.DATA_TYPE, AnyOfItemConditionType::new));
    public static final ConditionConfiguration<ConstantItemConditionType> CONSTANT = register(ConstantMetaConditionType.createConfiguration(ConstantItemConditionType::new));
    public static final ConditionConfiguration<RandomChanceItemConditionType> RANDOM_CHANCE = register(RandomChanceMetaConditionType.createConfiguration(RandomChanceItemConditionType::new));

    public static final ConditionConfiguration<AmountItemConditionType> AMOUNT = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("amount"), AmountItemConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<ArmorValueItemConditionType> ARMOR_VALUE = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("armor_value"), ArmorValueItemConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<CustomDataItemConditionType> CUSTOM_DATA = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("custom_data"), CustomDataItemConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<DurabilityItemConditionType> DURABILITY = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("durability"), DurabilityItemConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<EnchantmentItemConditionType> ENCHANTMENT = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("enchantment"), EnchantmentItemConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<EquippableItemConditionType> EQUIPPABLE = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("equippable"), EquippableItemConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<FoodItemConditionType> FOOD = register(ConditionConfiguration.simple(Apoli.identifier("food"), FoodItemConditionType::new));
    public static final ConditionConfiguration<FuelItemConditionType> FUEL = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("fuel"), FuelItemConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<HasPowerItemConditionType> HAS_POWER = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("has_power"), HasPowerItemConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<IngredientItemConditionType> INGREDIENT = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("ingredient"), IngredientItemConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<ItemCooldownItemConditionType> ITEM_COOLDOWN = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("item_cooldown"), ItemCooldownItemConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<PowerCountItemConditionType> POWER_COUNT = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("power_count"), PowerCountItemConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<RelativeDurabilityItemConditionType> RELATIVE_DURABILITY = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("relative_durability"), RelativeDurabilityItemConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<RelativeItemCooldownItemConditionType> RELATIVE_ITEM_COOLDOWN = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("relative_item_cooldown"), RelativeItemCooldownItemConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<SmeltableItemConditionType> SMELTABLE = register(ConditionConfiguration.simple(Apoli.identifier("smeltable"), SmeltableItemConditionType::new));

    public static void register() {

    }

    @SuppressWarnings("unchecked")
    public static <CT extends ItemConditionType> ConditionConfiguration<CT> register(ConditionConfiguration<CT> configuration) {

        ConditionConfiguration<ItemConditionType> casted = (ConditionConfiguration<ItemConditionType>) configuration;
        Registry.register(ApoliRegistries.ITEM_CONDITION_TYPE, casted.id(), casted);

        return configuration;

    }

}
