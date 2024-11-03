package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class ModifyFoodPowerType extends PowerType {

    public static final TypedDataObjectFactory<ModifyFoodPowerType> DATA_FACTORY = createConditionedDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("food_modifier", Modifier.DATA_TYPE, null)
            .addFunctionedDefault("food_modifiers", Modifier.LIST_TYPE, data -> MiscUtil.singletonListOrEmpty(data.get("food_modifier")))
            .add("saturation_modifier", Modifier.DATA_TYPE, null)
            .addFunctionedDefault("saturation_modifiers", Modifier.LIST_TYPE, data -> MiscUtil.singletonListOrEmpty(data.get("saturation_modifier")))
            .add("eat_ticks_modifier", Modifier.DATA_TYPE, null)
            .addFunctionedDefault("eat_ticks_modifiers", Modifier.LIST_TYPE, data -> MiscUtil.singletonListOrEmpty(data.get("eat_ticks_modifier")))
            .add("replace_stack", SerializableDataTypes.ITEM_STACK.optional(), Optional.empty())
            .add("prevent_effects", SerializableDataTypes.BOOLEAN, false)
            .add("always_edible", SerializableDataTypes.BOOLEAN, false)
            .addFunctionedDefault("can_always_eat", SerializableDataTypes.BOOLEAN, data -> data.getBoolean("always_edible")),
        (data, condition) -> new ModifyFoodPowerType(
            data.get("entity_action"),
            data.get("item_action"),
            data.get("item_condition"),
            data.get("food_modifiers"),
            data.get("saturation_modifiers"),
            data.get("eat_ticks_modifiers"),
            data.get("replace_stack"),
            data.get("prevent_effects"),
            data.get("can_always_eat"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_action", powerType.entityAction)
            .set("item_action", powerType.itemAction)
            .set("item_condition", powerType.itemCondition)
            .set("food_modifiers", powerType.foodModifiers)
            .set("saturaton_modifiers", powerType.saturationModifiers)
            .set("eat_ticks_modifiers", powerType.eatTicksModifiers)
            .set("replace_stack", powerType.replaceStack)
            .set("prevent_effects", powerType.preventFoodEffects)
            .set("can_always_eat", powerType.canAlwaysEat)
    );

    private final Optional<EntityAction> entityAction;
    private final Optional<ItemAction> itemAction;

    private final Optional<ItemCondition> itemCondition;

    private final List<Modifier> foodModifiers;
    private final List<Modifier> saturationModifiers;
    private final List<Modifier> eatTicksModifiers;

    private final Optional<ItemStack> replaceStack;

    private final boolean preventFoodEffects;
    private final boolean canAlwaysEat;

    public ModifyFoodPowerType(Optional<EntityAction> entityAction, Optional<ItemAction> itemAction, Optional<ItemCondition> itemCondition, List<Modifier> foodModifiers, List<Modifier> saturationModifiers, List<Modifier> eatTicksModifiers, Optional<ItemStack> replaceStack, boolean preventFoodEffects, boolean canAlwaysEat, Optional<EntityCondition> condition) {
        super(condition);

        this.entityAction = entityAction;
        this.itemAction = itemAction;

        this.itemCondition = itemCondition;

        this.foodModifiers = foodModifiers;
        this.saturationModifiers = saturationModifiers;
        this.eatTicksModifiers = eatTicksModifiers;

        this.replaceStack = replaceStack;

        this.preventFoodEffects = preventFoodEffects;
        this.canAlwaysEat = canAlwaysEat;

    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_FOOD;
    }

    public boolean doesApply(ItemStack stack) {
        return itemCondition
            .map(condition -> condition.test(getHolder().getWorld(), stack))
            .orElse(true);
    }

    public void setConsumedItemStackReference(StackReference stackReference) {
        replaceStack.ifPresent(stackReference::set);
        itemAction.ifPresent(action -> action.execute(getHolder().getWorld(), stackReference));
    }

    public void eat() {
        entityAction.ifPresent(action -> action.execute(getHolder()));
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
        return canAlwaysEat;
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

}
