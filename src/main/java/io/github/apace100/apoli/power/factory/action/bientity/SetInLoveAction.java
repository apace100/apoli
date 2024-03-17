package io.github.apace100.apoli.power.factory.action.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;

public class SetInLoveAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {

        if (actorAndTarget.getRight() instanceof AnimalEntity targetAnimal && actorAndTarget.getLeft() instanceof PlayerEntity actorPlayer) {
            targetAnimal.lovePlayer(actorPlayer);
        }

    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("set_in_love"),
            new SerializableData(),
            SetInLoveAction::action
        );
    }

}
