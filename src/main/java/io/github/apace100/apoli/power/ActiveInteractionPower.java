package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActiveInteractionPower extends InteractionPower implements Prioritized<ActiveInteractionPower> {

    private final int priority;

    public ActiveInteractionPower(PowerType<?> type, LivingEntity entity, EnumSet<Hand> hands, ActionResult actionResult, Predicate<Pair<World, ItemStack>> itemCondition, Consumer<Pair<World, ItemStack>> heldItemAction, ItemStack itemResult, Consumer<Pair<World, ItemStack>> resultItemAction, int priority) {
        super(type, entity, hands, actionResult, itemCondition, heldItemAction, itemResult, resultItemAction);
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
