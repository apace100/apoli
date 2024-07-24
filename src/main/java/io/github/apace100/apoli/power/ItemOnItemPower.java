package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.PriorityPhase;
import io.github.apace100.apoli.util.StackClickPhase;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemOnItemPower extends Power implements Prioritized<ItemOnItemPower> {

    private final Predicate<Pair<World, ItemStack>> usingItemCondition;
    private final Predicate<Pair<World, ItemStack>> onItemCondition;

    private final Consumer<Pair<World, StackReference>> usingItemAction;
    private final Consumer<Pair<World, StackReference>> onItemAction;
    private final Consumer<Pair<World, StackReference>> resultItemAction;
    private final Consumer<Entity> entityAction;

    private final EnumSet<ClickType> clickTypes;
    private final EnumSet<StackClickPhase> clickPhases;

    private final ItemStack resultStack;

    private final int resultFromOnStack;
    private final int priority;

    public ItemOnItemPower(PowerType<?> type, LivingEntity entity, EnumSet<ClickType> clickTypes, EnumSet<StackClickPhase> clickPhases, Consumer<Entity> entityAction, Consumer<Pair<World, StackReference>> usingItemAction, Consumer<Pair<World, StackReference>> onItemAction, Consumer<Pair<World, StackReference>> resultItemAction, Predicate<Pair<World, ItemStack>> usingItemCondition, Predicate<Pair<World, ItemStack>> onItemCondition, ItemStack resultStack, int resultFromOnStack, int priority) {
        super(type, entity);
        this.usingItemCondition = usingItemCondition;
        this.onItemCondition = onItemCondition;
        this.resultStack = resultStack;
        this.usingItemAction = usingItemAction;
        this.onItemAction = onItemAction;
        this.resultItemAction = resultItemAction;
        this.entityAction = entityAction;
        this.resultFromOnStack = resultFromOnStack;
        this.clickTypes = clickTypes;
        this.clickPhases = clickPhases;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean doesApply(ItemStack usingStack, ItemStack onStack, ClickType clickType, StackClickPhase clickPhase, PriorityPhase priorityPhase) {
        return clickTypes.contains(clickType)
            && clickPhases.contains(clickPhase)
            && priorityPhase.test(this.getPriority())
            && (onItemCondition == null || onItemCondition.test(new Pair<>(entity.getWorld(), onStack)))
            && (usingItemCondition == null || usingItemCondition.test(new Pair<>(entity.getWorld(), usingStack)));
    }

    public void execute(StackReference usingStackRef, StackReference onStackRef, Slot slot) {

        StackReference resultStackRef = InventoryUtil.createStackReference(resultStack != null
            ? resultStack.copy()
            : resultFromOnStack > 0
                ? onStackRef.get().split(resultFromOnStack)
                : onStackRef.get());

        if (resultItemAction != null) {
            resultItemAction.accept(new Pair<>(entity.getWorld(), resultStackRef));
        }

        if (usingItemAction != null) {
            usingItemAction.accept(new Pair<>(entity.getWorld(), usingStackRef));
        }

        if (onItemAction != null) {
            onItemAction.accept(new Pair<>(entity.getWorld(), onStackRef));
        }

        if (entity instanceof PlayerEntity player && (resultStack != null || resultItemAction != null)) {

            if (slot.hasStack()) {
                player.getInventory().offerOrDrop(resultStackRef.get());
            }

            else {
                slot.setStackNoCallbacks(resultStackRef.get());
            }

        }

        if (entityAction != null) {
            entityAction.accept(entity);
        }

    }

    public static boolean executeActions(PlayerEntity user, PriorityPhase priorityPhase, StackClickPhase clickPhase, ClickType clickType, Slot slot, StackReference slotStackReference, StackReference cursorStackReference) {

        CallInstance<ItemOnItemPower> ioipci = new CallInstance<>();
        ioipci.add(user, ItemOnItemPower.class, p -> p.doesApply(cursorStackReference.get(), slotStackReference.get(), clickType, clickPhase, priorityPhase));

        for (int i = ioipci.getMaxPriority(); i >= ioipci.getMinPriority(); i--) {
            ioipci.forEach(i, p -> p.execute(cursorStackReference, slotStackReference, slot));
        }

        return !ioipci.isEmpty();

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("item_on_item"),
            new SerializableData()
                .add("click_type", ApoliDataTypes.CLICK_TYPE, ClickType.RIGHT)
                .addFunctionedDefault("click_types", ApoliDataTypes.CLICK_TYPE_SET, data -> EnumSet.of(data.get("click_type")))
                .add("click_phases", ApoliDataTypes.STACK_CLICK_PHASE_SET, EnumSet.allOf(StackClickPhase.class))
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("using_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("on_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("result_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("using_item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("on_item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("result", SerializableDataTypes.ITEM_STACK, null)
                .addFunctionedDefault("result_stack", SerializableDataTypes.ITEM_STACK, data -> data.get("result"))
                .add("result_from_on_stack", SerializableDataTypes.INT, 0)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (powerType, livingEntity) -> new ItemOnItemPower(
                powerType,
                livingEntity,
                data.get("click_types"),
                data.get("click_phases"),
                data.get("entity_action"),
                data.get("using_item_action"),
                data.get("on_item_action"),
                data.get("result_item_action"),
                data.get("using_item_condition"),
                data.get("on_item_condition"),
                data.get("result_stack"),
                data.get("result_from_on_stack"),
                data.get("priority")
            )
        ).allowCondition();
    }
}
