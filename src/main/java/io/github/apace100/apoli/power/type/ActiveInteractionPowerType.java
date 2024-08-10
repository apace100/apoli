package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.power.Power;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActiveInteractionPowerType extends InteractionPowerType implements Prioritized<ActiveInteractionPowerType> {

    private final int priority;

    public ActiveInteractionPowerType(Power power, LivingEntity entity, EnumSet<Hand> hands, ActionResult actionResult, Predicate<Pair<World, ItemStack>> itemCondition, Consumer<Pair<World, StackReference>> heldItemAction, ItemStack itemResult, Consumer<Pair<World, StackReference>> resultItemAction, int priority) {
        super(power, entity, hands, actionResult, itemCondition, heldItemAction, itemResult, resultItemAction);
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

}
