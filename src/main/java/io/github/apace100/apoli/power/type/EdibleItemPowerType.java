package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
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
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;

public class EdibleItemPowerType extends PowerType implements Prioritized<EdibleItemPowerType> {

    public static final TypedDataObjectFactory<EdibleItemPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
            .add("result_item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("food_component", SerializableDataTypes.FOOD_COMPONENT)
            .add("result_stack", SerializableDataTypes.ITEM_STACK.optional(), Optional.empty())
            .add("consume_animation", SerializableDataType.enumValue(UseAction.class), UseAction.EAT)
            .add("consume_sound", SerializableDataTypes.SOUND_EVENT, SoundEvents.ENTITY_GENERIC_EAT)
            .add("priority", SerializableDataTypes.INT, 0),
        (data, condition) -> new EdibleItemPowerType(
            data.get("entity_action"),
            data.get("item_action"),
            data.get("result_item_action"),
            data.get("item_condition"),
            data.get("food_component"),
            data.get("result_stack"),
            data.get("consume_animation"),
            data.get("consume_sound"),
            data.get("priority"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_action", powerType.entityAction)
            .set("item_action", powerType.consumedItemAction)
            .set("result_item_action", powerType.resultItemAction)
            .set("item_condition", powerType.itemCondition)
            .set("food_component", powerType.foodComponent)
            .set("result_stack", powerType.resultStack)
            .set("consume_animation", powerType.consumeAnimation)
            .set("consume_sound", powerType.consumeSoundEvent)
            .set("priority", powerType.getPriority())
    );

    private final Optional<EntityAction> entityAction;
    private final Optional<ItemAction> resultItemAction;
    private final Optional<ItemAction> consumedItemAction;

    private final Optional<ItemCondition> itemCondition;

    private final FoodComponent foodComponent;
    private final Optional<ItemStack> resultStack;
    private final UseAction consumeAnimation;
    private final SoundEvent consumeSoundEvent;

    private final int priority;

    public EdibleItemPowerType(Optional<EntityAction> entityAction, Optional<ItemAction> consumedItemAction, Optional<ItemAction> resultItemAction, Optional<ItemCondition> itemCondition, FoodComponent foodComponent, Optional<ItemStack> resultStack, UseAction consumeAnimation, SoundEvent consumeSoundEvent, int priority, Optional<EntityCondition> condition) {
        super(condition);
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
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.EDIBLE_ITEM;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean doesApply(ItemStack stack) {
        return itemCondition
            .map(condition -> condition.test(getHolder().getWorld(), stack))
            .orElse(true);
    }

    public void executeEntityAction() {
        entityAction.ifPresent(action -> action.execute(getHolder()));
    }

    public StackReference executeItemActions(StackReference consumedStackReference) {

        LivingEntity holder = getHolder();
        World world = holder.getWorld();

        consumedItemAction.ifPresent(action -> action.execute(world, consumedStackReference));

        StackReference resultStackReference = this.resultStack
            .map(ItemStack::copy)
            .map(InventoryUtil::createStackReference)
            .orElse(StackReference.EMPTY);

        resultItemAction.ifPresent(action -> action.execute(world, resultStackReference));
        return resultStackReference;

    }

    public FoodComponent getFoodComponent() {
        return foodComponent;
    }

    public UseAction getConsumeAnimation() {
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
        Entity stackHolder = ((EntityLinkedItemStack) stack).apoli$getEntity(true);
        return get(stack, stackHolder);
    }

}
