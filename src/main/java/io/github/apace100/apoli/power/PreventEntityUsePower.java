package io.github.apace100.apoli.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PreventEntityUsePower extends InteractionPower {

    private final Consumer<Pair<Entity, Entity>> biEntityAction;
    private final Predicate<Pair<Entity, Entity>> bientityCondition;

    public PreventEntityUsePower(PowerType<?> type, LivingEntity entity, EnumSet<Hand> hands, ActionResult actionResult, Predicate<ItemStack> itemCondition, Consumer<Pair<World, ItemStack>> heldItemAction, ItemStack itemResult, Consumer<Pair<World, ItemStack>> itemAction, Consumer<Pair<Entity, Entity>> biEntityAction, Predicate<Pair<Entity, Entity>> bientityCondition) {
        super(type, entity, hands, actionResult, itemCondition, heldItemAction, itemResult, itemAction);
        this.biEntityAction = biEntityAction;
        this.bientityCondition = bientityCondition;
    }


    public boolean doesApply(Entity other, Hand hand, ItemStack heldStack) {
        if(!shouldExecute(hand, heldStack)) {
            return false;
        }
        return bientityCondition == null || bientityCondition.test(new Pair<>(entity, other));
    }

    public ActionResult executeAction(Entity other, Hand hand) {
        if(biEntityAction != null) {
            biEntityAction.accept(new Pair<>(entity, other));
        }
        performActorItemStuff(this, (PlayerEntity) entity, hand);
        return getActionResult();
    }
}
