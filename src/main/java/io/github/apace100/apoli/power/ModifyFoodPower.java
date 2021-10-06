package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyFoodPower extends Power {

    private final Predicate<ItemStack> applicableFood;
    private final List<EntityAttributeModifier> foodModifiers;
    private final List<EntityAttributeModifier> saturationModifiers;
    private final Consumer<Entity> entityActionWhenEaten;
    private final boolean makeAlwaysEdible;

    public ModifyFoodPower(PowerType<?> type, LivingEntity entity, Predicate<ItemStack> applicableFood, List<EntityAttributeModifier> foodModifiers, List<EntityAttributeModifier> saturationModifiers, Consumer<Entity> entityActionWhenEaten, boolean makeAlwaysEdible) {
        super(type, entity);
        this.applicableFood = applicableFood;
        this.foodModifiers = foodModifiers;
        this.saturationModifiers = saturationModifiers;
        this.entityActionWhenEaten = entityActionWhenEaten;
        this.makeAlwaysEdible = makeAlwaysEdible;
    }

    public boolean doesApply(ItemStack stack) {
        return applicableFood.test(stack);
    }

    public void eat() {
        if(entityActionWhenEaten != null) {
            entityActionWhenEaten.accept(entity);
        }
    }

    public List<EntityAttributeModifier> getFoodModifiers() {
        return foodModifiers;
    }

    public List<EntityAttributeModifier> getSaturationModifiers() {
        return saturationModifiers;
    }

    public boolean doesMakeAlwaysEdible() {
        return makeAlwaysEdible;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_food"),
            new SerializableData()
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("food_modifier", SerializableDataTypes.ATTRIBUTE_MODIFIER, null)
                .add("food_modifiers", SerializableDataTypes.ATTRIBUTE_MODIFIERS, null)
                .add("saturation_modifier", SerializableDataTypes.ATTRIBUTE_MODIFIER, null)
                .add("saturation_modifiers", SerializableDataTypes.ATTRIBUTE_MODIFIERS, null)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("always_edible", SerializableDataTypes.BOOLEAN, false),
            data ->
                (type, player) -> {
                    List<EntityAttributeModifier> foodModifiers = new LinkedList<>();
                    List<EntityAttributeModifier> saturationModifiers = new LinkedList<>();
                    if(data.isPresent("food_modifier")) {
                        foodModifiers.add((EntityAttributeModifier)data.get("food_modifier"));
                    }
                    if(data.isPresent("food_modifiers")) {
                        List<EntityAttributeModifier> modifierList = (List<EntityAttributeModifier>)data.get("food_modifiers");
                        foodModifiers.addAll(modifierList);
                    }
                    if(data.isPresent("saturation_modifier")) {
                        saturationModifiers.add((EntityAttributeModifier)data.get("saturation_modifier"));
                    }
                    if(data.isPresent("saturation_modifiers")) {
                        List<EntityAttributeModifier> modifierList = (List<EntityAttributeModifier>)data.get("saturation_modifiers");
                        saturationModifiers.addAll(modifierList);
                    }
                    return new ModifyFoodPower(type, player, data.isPresent("item_condition") ? (ConditionFactory<ItemStack>.Instance)data.get("item_condition") : stack -> true,
                        foodModifiers, saturationModifiers, (ActionFactory<Entity>.Instance)data.get("entity_action"), data.getBoolean("always_edible"));
                }).allowCondition();
    }
}
