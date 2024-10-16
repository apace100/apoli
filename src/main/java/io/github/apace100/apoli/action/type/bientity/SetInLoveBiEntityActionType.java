package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

public class SetInLoveBiEntityActionType extends BiEntityActionType {

    @Override
	protected void execute(Entity actor, Entity target) {

        if (target instanceof AnimalEntity targetAnimal && actor instanceof PlayerEntity actorPlayer) {
            targetAnimal.lovePlayer(actorPlayer);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BiEntityActionTypes.SET_IN_LOVE;
    }

}
