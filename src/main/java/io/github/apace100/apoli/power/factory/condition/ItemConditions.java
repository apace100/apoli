package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.StackPowerUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class ItemConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Apoli.identifier("constant"), new SerializableData()
            .add("value", SerializableDataTypes.BOOLEAN),
            (data, stack) -> data.getBoolean("value")));
        register(new ConditionFactory<>(Apoli.identifier("and"), new SerializableData()
            .add("conditions", ApoliDataTypes.ITEM_CONDITIONS),
            (data, stack) -> ((List<ConditionFactory<ItemStack>.Instance>)data.get("conditions")).stream().allMatch(
                condition -> condition.test(stack)
            )));
        register(new ConditionFactory<>(Apoli.identifier("or"), new SerializableData()
            .add("conditions", ApoliDataTypes.ITEM_CONDITIONS),
            (data, stack) -> ((List<ConditionFactory<ItemStack>.Instance>)data.get("conditions")).stream().anyMatch(
                condition -> condition.test(stack)
            )));
        register(new ConditionFactory<>(Apoli.identifier("food"), new SerializableData(),
            (data, stack) -> stack.isFood()));
        register(new ConditionFactory<>(Apoli.identifier("ingredient"), new SerializableData()
            .add("ingredient", SerializableDataTypes.INGREDIENT),
            (data, stack) -> ((Ingredient)data.get("ingredient")).test(stack)));
        register(new ConditionFactory<>(Apoli.identifier("armor_value"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, stack) -> {
                int armor = 0;
                if(stack.getItem() instanceof ArmorItem) {
                    ArmorItem item = (ArmorItem)stack.getItem();
                    armor = item.getProtection();
                }
                return ((Comparison)data.get("comparison")).compare(armor, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("harvest_level"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, stack) -> {
                int harvestLevel = 0;
                if(stack.getItem() instanceof ToolItem) {
                    ToolItem item = (ToolItem)stack.getItem();
                    harvestLevel = item.getMaterial().getMiningLevel();
                }
                return ((Comparison)data.get("comparison")).compare(harvestLevel, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("enchantment"), new SerializableData()
            .add("enchantment", SerializableDataTypes.ENCHANTMENT)
            .add("compare_to", SerializableDataTypes.INT)
            .add("comparison", ApoliDataTypes.COMPARISON),
            (data, stack) -> {
                int enchantLevel = EnchantmentHelper.getLevel(data.get("enchantment"), stack);
                return ((Comparison)data.get("comparison")).compare(enchantLevel, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("meat"), new SerializableData(),
            (data, stack) -> stack.isFood() && stack.getItem().getFoodComponent().isMeat()));
        register(new ConditionFactory<>(Apoli.identifier("nbt"), new SerializableData()
            .add("nbt", SerializableDataTypes.NBT), (data, stack) -> NbtHelper.matches(data.get("nbt"), stack.getNbt(), true)));
        register(new ConditionFactory<>(Apoli.identifier("fireproof"), new SerializableData(),
            (data, stack) -> stack.getItem().isFireproof()));
        register(new ConditionFactory<>(Apoli.identifier("enchantable"), new SerializableData(),
            (data, stack) -> !stack.isEnchantable()));
        register(new ConditionFactory<>(Apoli.identifier("power_count"), new SerializableData()
            .add("slot", SerializableDataTypes.EQUIPMENT_SLOT, null)
            .add("compare_to", SerializableDataTypes.INT)
            .add("comparison", ApoliDataTypes.COMPARISON),
            (data, stack) -> {
                int totalCount = 0;
                if(data.isPresent("slot")) {
                    totalCount = StackPowerUtil.getPowers(stack, data.get("slot")).size();
                } else {
                    for (EquipmentSlot slot :
                        EquipmentSlot.values()) {
                        totalCount += StackPowerUtil.getPowers(stack, slot).size();
                    }
                }
                return ((Comparison)data.get("comparison")).compare(totalCount, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("has_power"), new SerializableData()
            .add("slot", SerializableDataTypes.EQUIPMENT_SLOT, null)
            .add("power", SerializableDataTypes.IDENTIFIER),
            (data, stack) -> {
                Identifier power = data.getId("power");
                if(data.isPresent("slot")) {
                    return StackPowerUtil.getPowers(stack, data.get("slot")).stream().anyMatch(p -> p.powerId.equals(power));
                } else {
                    for (EquipmentSlot slot :
                        EquipmentSlot.values()) {
                        if(StackPowerUtil.getPowers(stack, slot).stream().anyMatch(p -> p.powerId.equals(power))) {
                            return true;
                        }
                    }
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("empty"), new SerializableData(),
            (data, stack) -> stack.isEmpty()));
        register(new ConditionFactory<>(Apoli.identifier("amount"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, stack) -> ((Comparison)data.get("comparison")).compare(stack.getCount(), data.getInt("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("is_damageable"), new SerializableData(),
            (data, stack) -> stack.isDamageable()));
        register(new ConditionFactory<>(Apoli.identifier("durability"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, stack) -> ((Comparison)data.get("comparison")).compare(stack.getMaxDamage() - stack.getDamage(), data.getInt("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("relative_durability"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, stack) -> ((Comparison)data.get("comparison")).compare((float)(stack.getMaxDamage() - stack.getDamage()) / stack.getMaxDamage(), data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("is_equippable"), new SerializableData()
            .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT),
            (data, stack) -> MobEntity.getPreferredEquipmentSlot(stack) == data.get("equipment_slot")));
    }

    private static void register(ConditionFactory<ItemStack> conditionFactory) {
        Registry.register(ApoliRegistries.ITEM_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
