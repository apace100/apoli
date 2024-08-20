package io.github.apace100.apoli.action.type.bientity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

public class SetInLoveActionType {

    public static void action(Entity actor, Entity target) {

        if (target instanceof AnimalEntity targetAnimal && actor instanceof PlayerEntity actorPlayer) {
            targetAnimal.lovePlayer(actorPlayer);
        }

    }

}
