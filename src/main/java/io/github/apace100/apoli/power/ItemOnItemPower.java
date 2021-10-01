package io.github.apace100.apoli.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemOnItemPower extends Power {

    private final Predicate<ItemStack> usingItemCondition;
    private final Predicate<ItemStack> onItemCondition;

    private final ItemStack newStack;
    private final Consumer<Pair<World, ItemStack>> usingItemAction;
    private final Consumer<Pair<World, ItemStack>> onItemAction;
    private final Consumer<Pair<World, ItemStack>> resultItemAction;
    private final Consumer<Entity> entityAction;

    public ItemOnItemPower(PowerType<?> type, LivingEntity entity, Predicate<ItemStack> usingItemCondition, Predicate<ItemStack> onItemCondition, ItemStack newStack, Consumer<Pair<World, ItemStack>> usingItemAction, Consumer<Pair<World, ItemStack>> onItemAction, Consumer<Pair<World, ItemStack>> resultItemAction, Consumer<Entity> entityAction) {
        super(type, entity);
        this.usingItemCondition = usingItemCondition;
        this.onItemCondition = onItemCondition;
        this.newStack = newStack;
        this.usingItemAction = usingItemAction;
        this.onItemAction = onItemAction;
        this.resultItemAction = resultItemAction;
        this.entityAction = entityAction;
    }

    public boolean doesApply(ItemStack using, ItemStack on) {
        if(usingItemCondition != null && !usingItemCondition.test(using)) {
            return false;
        }
        if(onItemCondition != null && !onItemCondition.test(on)) {
            return false;
        }
        return true;
    }

    public ItemStack execute(ItemStack using, ItemStack on, Slot slot) {
        ItemStack stack;
        if(newStack != null) {
            stack = newStack.copy();
            if(resultItemAction != null) {
                resultItemAction.accept(new Pair<>(entity.world, stack));
            }
        } else {
            stack = on;
        }
        if(usingItemAction != null) {
            usingItemAction.accept(new Pair<>(entity.world, using));
        }
        if(onItemAction != null) {
            onItemAction.accept(new Pair<>(entity.world, on));
        }
        if(newStack != null) {
            PlayerEntity player = (PlayerEntity)entity;
            if(slot.getStack().isEmpty()) {
                slot.setStack(stack);
            } else {
                player.getInventory().offerOrDrop(stack);
            }
        }
        if(entityAction != null) {
            entityAction.accept(entity);
        }
        return stack;
    }
}
