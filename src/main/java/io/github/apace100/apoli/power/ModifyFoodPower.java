package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyFoodPower extends Power {

    private final Predicate<Pair<World, ItemStack>> applicableFood;
    private final ItemStack replaceStack;
    private final Consumer<Pair<World, ItemStack>> consumableAction;
    private final List<Modifier> foodModifiers;
    private final List<Modifier> saturationModifiers;
    private final Consumer<Entity> entityActionWhenEaten;
    private final boolean preventFoodEffects;
    private final boolean makeAlwaysEdible;

    public ModifyFoodPower(PowerType<?> type, LivingEntity entity, Predicate<Pair<World, ItemStack>> applicableFood, ItemStack replaceStack, Consumer<Pair<World, ItemStack>> consumableAction, Modifier foodModifier, List<Modifier> foodModifiers, Modifier saturationModifier, List<Modifier> saturationModifiers, Consumer<Entity> entityActionWhenEaten, boolean makeAlwaysEdible, boolean preventFoodEffects) {

        super(type, entity);

        this.applicableFood = applicableFood;
        this.replaceStack = replaceStack;
        this.consumableAction = consumableAction;

        this.foodModifiers = new LinkedList<>();
        if (foodModifier != null) this.foodModifiers.add(foodModifier);
        if (foodModifiers != null) this.foodModifiers.addAll(foodModifiers);

        this.saturationModifiers = new LinkedList<>();
        if (saturationModifier != null) this.saturationModifiers.add(saturationModifier);
        if (saturationModifiers != null) this.saturationModifiers.addAll(saturationModifiers);

        this.entityActionWhenEaten = entityActionWhenEaten;
        this.makeAlwaysEdible = makeAlwaysEdible;
        this.preventFoodEffects = preventFoodEffects;

    }

    public boolean doesApply(ItemStack stack) {
        return applicableFood == null || applicableFood.test(new Pair<>(entity.getWorld(), stack));
    }

    public ItemStack getConsumedItemStack(ItemStack stack) {

        if(replaceStack != null) stack = replaceStack;
        ItemStack consumed = stack.copy();

        if(consumableAction != null) consumableAction.accept(new Pair<>(entity.getWorld(), consumed));
        return consumed;

    }

    public void eat() {
        if (entityActionWhenEaten != null) entityActionWhenEaten.accept(entity);
    }

    public List<Modifier> getFoodModifiers() {
        return foodModifiers;
    }

    public List<Modifier> getSaturationModifiers() {
        return saturationModifiers;
    }

    public boolean doesMakeAlwaysEdible() {
        return makeAlwaysEdible;
    }

    public boolean doesPreventEffects() {
        return preventFoodEffects;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_food"),
            new SerializableData()
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("replace_stack", SerializableDataTypes.ITEM_STACK, null)
                .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("food_modifier", Modifier.DATA_TYPE, null)
                .add("food_modifiers", Modifier.LIST_TYPE, null)
                .add("saturation_modifier", Modifier.DATA_TYPE, null)
                .add("saturation_modifiers", Modifier.LIST_TYPE, null)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("always_edible", SerializableDataTypes.BOOLEAN, false)
                .add("prevent_effects", SerializableDataTypes.BOOLEAN, false),
            data -> (powerType, livingEntity) -> new ModifyFoodPower(
                powerType,
                livingEntity,
                data.get("item_condition"),
                data.get("replace_stack"),
                data.get("item_action"),
                data.get("food_modifier"),
                data.get("food_modifiers"),
                data.get("saturation_modifier"),
                data.get("saturation_modifiers"),
                data.get("entity_action"),
                data.get("always_edible"),
                data.get("prevent_effects")
            )
        ).allowCondition();
    }
}
