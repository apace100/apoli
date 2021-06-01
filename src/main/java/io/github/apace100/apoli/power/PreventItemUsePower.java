package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class PreventItemUsePower extends Power {

    private final Predicate<ItemStack> predicate;

    public PreventItemUsePower(PowerType<?> type, LivingEntity entity, Predicate<ItemStack> predicate) {
        super(type, entity);
        this.predicate = predicate;
        /*UseItemCallback.EVENT.register(((playerEntity, world, hand) -> {
            if(getType().isActive(playerEntity)) {
                ItemStack stackInHand = playerEntity.getStackInHand(hand);
                if(doesPrevent(stackInHand)) {
                    return TypedActionResult.fail(stackInHand);
                }
            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        }));*/
    }

    public boolean doesPrevent(ItemStack stack) {
        return predicate.test(stack);
    }
}
