package io.github.apace100.apoli.action.type.bientity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;

public class TameActionType {

    public static void action(Entity actor, Entity target) {

        if (!(actor instanceof PlayerEntity actorPlayer)) {
            return;
        }

        if (target instanceof TameableEntity tameableTarget && !tameableTarget.isTamed()) {
            tameableTarget.setOwner(actorPlayer);
        }

        else if (target instanceof AbstractHorseEntity targetHorseLike && !targetHorseLike.isTame()) {
            targetHorseLike.bondWithPlayer(actorPlayer);
        }

    }

}
