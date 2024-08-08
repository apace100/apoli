package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyFoodPowerType extends PowerType {

    private final Predicate<Pair<World, ItemStack>> itemCondition;
    private final ItemStack replaceStack;
    private final Consumer<Pair<World, StackReference>> itemAction;
    private final List<Modifier> foodModifiers;
    private final List<Modifier> saturationModifiers;
    private final List<Modifier> eatTicksModifiers;
    private final Consumer<Entity> entityActionWhenEaten;
    private final boolean preventFoodEffects;
    private final boolean makeAlwaysEdible;

    public ModifyFoodPowerType(Power power, LivingEntity entity, Predicate<Pair<World, ItemStack>> itemCondition, ItemStack replaceStack, Consumer<Pair<World, StackReference>> itemAction, Modifier foodModifier, List<Modifier> foodModifiers, Modifier saturationModifier, List<Modifier> saturationModifiers, Modifier eatSecondsModifier, List<Modifier> eatTicksModifiers, Consumer<Entity> entityActionWhenEaten, boolean makeAlwaysEdible, boolean preventFoodEffects) {

        super(power, entity);

        this.itemCondition = itemCondition;
        this.replaceStack = replaceStack;
        this.itemAction = itemAction;

        this.foodModifiers = new LinkedList<>();

        if (foodModifier != null) {
            this.foodModifiers.add(foodModifier);
        }

        if (foodModifiers != null) {
            this.foodModifiers.addAll(foodModifiers);
        }

        this.saturationModifiers = new LinkedList<>();

        if (saturationModifier != null) {
            this.saturationModifiers.add(saturationModifier);
        }

        if (saturationModifiers != null) {
            this.saturationModifiers.addAll(saturationModifiers);
        }

        this.eatTicksModifiers = new LinkedList<>();

        if (eatSecondsModifier != null) {
            this.eatTicksModifiers.add(eatSecondsModifier);
        }

        if (eatTicksModifiers != null) {
            this.eatTicksModifiers.addAll(eatTicksModifiers);
        }

        this.entityActionWhenEaten = entityActionWhenEaten;
        this.makeAlwaysEdible = makeAlwaysEdible;
        this.preventFoodEffects = preventFoodEffects;

    }

    public boolean doesApply(ItemStack stack) {
        return itemCondition == null || itemCondition.test(new Pair<>(entity.getWorld(), stack));
    }

    public void setConsumedItemStackReference(StackReference stack) {

        if(replaceStack != null) {
            stack.set(this.replaceStack);
        }

        if(itemAction != null) {
            itemAction.accept(new Pair<>(entity.getWorld(), stack));
        }

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

    public List<Modifier> getEatTicksModifiers() {
        return eatTicksModifiers;
    }

    public boolean doesMakeAlwaysEdible() {
        return makeAlwaysEdible;
    }

    public boolean doesPreventEffects() {
        return preventFoodEffects;
    }

    public static OptionalInt modifyEatTicks(@Nullable Entity entity, ItemStack stack) {

        FoodComponent foodComponent = EdibleItemPowerType.get(stack)
            .map(EdibleItemPowerType::getFoodComponent)
            .orElseGet(() -> stack.get(DataComponentTypes.FOOD));

        if (foodComponent == null) {
            return OptionalInt.empty();
        }

        List<Modifier> modifiers = PowerHolderComponent.getPowerTypes(entity, ModifyFoodPowerType.class)
            .stream()
            .filter(p -> p.doesApply(stack))
            .flatMap(p -> p.getEatTicksModifiers().stream())
            .toList();

        return OptionalInt.of((int) ModifierUtil.applyModifiers(entity, modifiers, foodComponent.getEatTicks()));

    }

    public static PowerTypeFactory<ModifyFoodPowerType> getFactory() {
        return new PowerTypeFactory<>(Apoli.identifier("modify_food"),
            new SerializableData()
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("replace_stack", SerializableDataTypes.ITEM_STACK, null)
                .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("food_modifier", Modifier.DATA_TYPE, null)
                .add("food_modifiers", Modifier.LIST_TYPE, null)
                .add("saturation_modifier", Modifier.DATA_TYPE, null)
                .add("saturation_modifiers", Modifier.LIST_TYPE, null)
                .add("eat_ticks_modifier", Modifier.DATA_TYPE, null)
                .add("eat_ticks_modifiers", Modifier.LIST_TYPE, null)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("always_edible", SerializableDataTypes.BOOLEAN, false)
                .add("prevent_effects", SerializableDataTypes.BOOLEAN, false),
            data -> (power, entity) -> new ModifyFoodPowerType(power, entity,
                data.get("item_condition"),
                data.get("replace_stack"),
                data.get("item_action"),
                data.get("food_modifier"),
                data.get("food_modifiers"),
                data.get("saturation_modifier"),
                data.get("saturation_modifiers"),
                data.get("eat_ticks_modifier"),
                data.get("eat_ticks_modifiers"),
                data.get("entity_action"),
                data.get("always_edible"),
                data.get("prevent_effects")
            )
        ).allowCondition();
    }
}
