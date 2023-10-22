package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EdibleItemPower extends Power implements Prioritized<EdibleItemPower> {

    private final Consumer<Entity> entityAction;
    private final Consumer<Pair<World, ItemStack>> resultItemAction;
    private final Consumer<Pair<World, ItemStack>> consumedItemAction;

    private final Predicate<Pair<World, ItemStack>> itemCondition;

    private final List<Modifier> consumingTimeModifiers;
    private final FoodComponent foodComponent;
    private final ItemStack resultStack;
    private final ConsumeAnimation consumeAnimation;
    private final SoundEvent consumeSoundEvent;

    private final int priority;

    public EdibleItemPower(PowerType<?> powerType, LivingEntity livingEntity, Consumer<Entity> entityAction, Consumer<Pair<World, ItemStack>> consumedItemAction, Consumer<Pair<World, ItemStack>> resultItemAction, Predicate<Pair<World, ItemStack>> itemCondition, FoodComponent foodComponent, ItemStack resultStack, ConsumeAnimation consumeAnimation, SoundEvent consumeSoundEvent, Modifier consumingTimeModifier, List<Modifier> consumingTimeModifiers, int priority) {
        super(powerType, livingEntity);

        this.entityAction = entityAction;
        this.consumedItemAction = consumedItemAction;
        this.resultItemAction = resultItemAction;
        this.itemCondition = itemCondition;
        this.foodComponent = foodComponent;
        this.resultStack = resultStack;
        this.consumeAnimation = consumeAnimation;
        this.consumeSoundEvent = consumeSoundEvent;

        this.consumingTimeModifiers = new LinkedList<>();
        if (consumingTimeModifier != null) {
            this.consumingTimeModifiers.add(consumingTimeModifier);
        }

        if (consumingTimeModifiers != null) {
            this.consumingTimeModifiers.addAll(consumingTimeModifiers);
        }

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

    public ItemStack executeItemActions(ItemStack consumedStack) {

        if (consumedItemAction != null) {
            consumedItemAction.accept(new Pair<>(entity.getWorld(), consumedStack));
        }

        ItemStack resultStack = this.resultStack != null ? this.resultStack.copy() : null;
        if (resultStack != null && resultItemAction != null) {
            resultItemAction.accept(new Pair<>(entity.getWorld(), resultStack));
        }

        return resultStack;

    }

    public void applyEffects() {

        if (entity.getWorld().isClient) {
            return;
        }

        foodComponent.getStatusEffects()
            .stream()
            .filter(pair -> entity.getWorld().getRandom().nextFloat() < pair.getSecond())
            .forEach(pair -> entity.addStatusEffect(new StatusEffectInstance(pair.getFirst())));

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

    public int getConsumingTime() {
        return (int) ModifierUtil.applyModifiers(entity, consumingTimeModifiers, this.getFoodComponent().isSnack() ? 16 : 32);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
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
                .add("consuming_time_modifier", Modifier.DATA_TYPE, null)
                .add("consuming_time_modifiers", Modifier.LIST_TYPE, null)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (powerType, livingEntity) -> new EdibleItemPower(
                powerType,
                livingEntity,
                data.get("entity_action"),
                data.get("item_action"),
                data.get("result_item_action"),
                data.get("item_condition"),
                data.get("food_component"),
                data.get("result_stack"),
                data.get("consume_animation"),
                data.get("consume_sound"),
                data.get("consuming_time_modifier"),
                data.get("consuming_time_modifiers"),
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
