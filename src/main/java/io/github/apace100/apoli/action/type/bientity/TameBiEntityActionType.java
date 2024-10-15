package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;

public class TameBiEntityActionType extends BiEntityActionType {

    @Override
    public void execute(Entity actor, Entity target) {

        if (actor instanceof PlayerEntity actorPlayer) {

            if (target instanceof TameableEntity tameableTarget && !tameableTarget.isTamed()) {
                tameableTarget.setOwner(actorPlayer);
            }

            else if (target instanceof AbstractHorseEntity targetHorseLike && !targetHorseLike.isTame()) {
                targetHorseLike.bondWithPlayer(actorPlayer);
            }

        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BiEntityActionTypes.TAME;
    }

}
