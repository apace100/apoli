package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemOnItemPower extends Power {

    private final Predicate<Pair<World, ItemStack>> usingItemCondition;
    private final Predicate<Pair<World, ItemStack>> onItemCondition;

    private final int resultFromOnStack;
    private final ItemStack newStack;
    private final ClickType clickType;
    private final Consumer<Pair<World, ItemStack>> usingItemAction;
    private final Consumer<Pair<World, ItemStack>> onItemAction;
    private final Consumer<Pair<World, ItemStack>> resultItemAction;
    private final Consumer<Entity> entityAction;

    public ItemOnItemPower(PowerType<?> type, LivingEntity entity, Predicate<Pair<World, ItemStack>> usingItemCondition, Predicate<Pair<World, ItemStack>> onItemCondition, ItemStack newStack, Consumer<Pair<World, ItemStack>> usingItemAction, Consumer<Pair<World, ItemStack>> onItemAction, Consumer<Pair<World, ItemStack>> resultItemAction, Consumer<Entity> entityAction, int resultFromOnStack, ClickType clickType) {
        super(type, entity);
        this.usingItemCondition = usingItemCondition;
        this.onItemCondition = onItemCondition;
        this.newStack = newStack;
        this.usingItemAction = usingItemAction;
        this.onItemAction = onItemAction;
        this.resultItemAction = resultItemAction;
        this.entityAction = entityAction;
        this.resultFromOnStack = resultFromOnStack;
        this.clickType = clickType;
    }

    public boolean doesApply(ItemStack usingStack, ItemStack onStack, ClickType clickType) {
        return this.clickType == clickType
            && (onItemCondition == null || onItemCondition.test(new Pair<>(entity.getWorld(), onStack)))
            && (usingItemCondition == null || usingItemCondition.test(new Pair<>(entity.getWorld(), usingStack)));
    }

    public void execute(ItemStack usingStack, ItemStack onStack, Slot slot) {

        ItemStack resultStack = newStack != null ? newStack.copy()
                                                 : resultFromOnStack > 0 ? onStack.split(resultFromOnStack) : onStack;

        if (resultItemAction != null) {
            resultItemAction.accept(new Pair<>(entity.getWorld(), resultStack));
        }

        if (usingItemAction != null) {
            usingItemAction.accept(new Pair<>(entity.getWorld(), usingStack));
        }

        if (onItemAction != null) {
            onItemAction.accept(new Pair<>(entity.getWorld(), onStack));
        }

        tryOffer:
        if (newStack != null || resultItemAction != null) {

            if (!(entity instanceof PlayerEntity playerEntity)) {
                break tryOffer;
            }

            if (slot.hasStack()) {
                playerEntity.getInventory().offerOrDrop(resultStack);
            } else {
                slot.setStackNoCallbacks(resultStack);
            }

        }

        if (entityAction != null) {
            entityAction.accept(entity);
        }

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("item_on_item"),
            new SerializableData()
                .add("click_type", ApoliDataTypes.CLICK_TYPE, ClickType.RIGHT)
                .add("using_item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("on_item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("result_from_on_stack", SerializableDataTypes.INT, 0)
                .add("result", SerializableDataTypes.ITEM_STACK, null)
                .add("using_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("on_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("result_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
            data -> (powerType, livingEntity) -> new ItemOnItemPower(
                powerType,
                livingEntity,
                data.get("using_item_condition"),
                data.get("on_item_condition"),
                data.get("result"),
                data.get("using_item_action"),
                data.get("on_item_action"),
                data.get("result_item_action"),
                data.get("entity_action"),
                data.get("result_from_on_stack"),
                data.get("click_type")
            )
        ).allowCondition();
    }
}
