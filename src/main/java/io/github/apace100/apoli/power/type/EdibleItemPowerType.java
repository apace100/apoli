package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EdibleItemPowerType extends PowerType implements Prioritized<EdibleItemPowerType> {

    private final Consumer<Entity> entityAction;
    private final Consumer<Pair<World, StackReference>> resultItemAction;
    private final Consumer<Pair<World, StackReference>> consumedItemAction;

    private final Predicate<Pair<World, ItemStack>> itemCondition;

    private final FoodComponent foodComponent;
    private final ItemStack resultStack;
    private final ConsumeAnimation consumeAnimation;
    private final SoundEvent consumeSoundEvent;

    private final int priority;

    public EdibleItemPowerType(Power power, LivingEntity entity, Consumer<Entity> entityAction, Consumer<Pair<World, StackReference>> consumedItemAction, Consumer<Pair<World, StackReference>> resultItemAction, Predicate<Pair<World, ItemStack>> itemCondition, FoodComponent foodComponent, ItemStack resultStack, ConsumeAnimation consumeAnimation, SoundEvent consumeSoundEvent, int priority) {
        super(power, entity);
        this.entityAction = entityAction;
        this.consumedItemAction = consumedItemAction;
        this.resultItemAction = resultItemAction;
        this.itemCondition = itemCondition;
        this.foodComponent = foodComponent;
        this.resultStack = resultStack;
        this.consumeAnimation = consumeAnimation;
        this.consumeSoundEvent = consumeSoundEvent;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean doesApply(ItemStack stack) {
        return itemCondition == null || itemCondition.test(new Pair<>(entity.getWorld(), stack));
    }

    public void executeEntityAction() {
        if (entityAction != null) {
            entityAction.accept(entity);
        }
    }

    public StackReference executeItemActions(StackReference consumedStack) {

        if (consumedItemAction != null) {
            consumedItemAction.accept(new Pair<>(entity.getWorld(), consumedStack));
        }

        StackReference resultStack = this.resultStack != null ? InventoryUtil.createStackReference(this.resultStack.copy()) : StackReference.EMPTY;
        if (resultStack != StackReference.EMPTY && resultItemAction != null) {
            resultItemAction.accept(new Pair<>(entity.getWorld(), resultStack));
        }

        return resultStack;

    }

    public FoodComponent getFoodComponent() {
        return foodComponent;
    }

    public ConsumeAnimation getConsumeAnimation() {
        return consumeAnimation;
    }

    public SoundEvent getConsumeSoundEvent() {
        return consumeSoundEvent;
    }

    public static Optional<EdibleItemPowerType> get(ItemStack stack, @Nullable Entity holder) {
        return PowerHolderComponent.getPowerTypes(holder, EdibleItemPowerType.class)
            .stream()
            .filter(p -> p.doesApply(stack))
            .max(Comparator.comparing(EdibleItemPowerType::getPriority))
            .filter(p -> !stack.contains(DataComponentTypes.FOOD) || p.getPriority() > 1);
    }

    public static Optional<EdibleItemPowerType> get(ItemStack stack) {
        Entity stackHolder = ((EntityLinkedItemStack) stack).apoli$getEntity();
        return get(stack, stackHolder);
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("edible_item"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("result_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("food_component", SerializableDataTypes.FOOD_COMPONENT)
                .add("return_stack", SerializableDataTypes.ITEM_STACK, null)
                .addFunctionedDefault("result_stack", SerializableDataTypes.ITEM_STACK, data -> data.get("return_stack"))
                .add("use_action", SerializableDataType.enumValue(ConsumeAnimation.class), ConsumeAnimation.EAT)
                .addFunctionedDefault("consume_animation", SerializableDataType.enumValue(ConsumeAnimation.class), data -> data.get("use_action"))
                .add("sound", SerializableDataTypes.SOUND_EVENT, SoundEvents.ENTITY_GENERIC_EAT)
                .addFunctionedDefault("consume_sound", SerializableDataTypes.SOUND_EVENT, data -> data.get("sound"))
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (power, entity) -> new EdibleItemPowerType(
                power,
                entity,
                data.get("entity_action"),
                data.get("item_action"),
                data.get("result_item_action"),
                data.get("item_condition"),
                data.get("food_component"),
                data.get("result_stack"),
                data.get("consume_animation"),
                data.get("consume_sound"),
                data.get("priority")
            )
        ).allowCondition();
    }

    public enum ConsumeAnimation {

        EAT(UseAction.EAT),
        DRINK(UseAction.DRINK);

        final UseAction action;
        ConsumeAnimation(UseAction action) {
            this.action = action;
        }

        public UseAction getAction() {
            return action;
        }

    }

}
