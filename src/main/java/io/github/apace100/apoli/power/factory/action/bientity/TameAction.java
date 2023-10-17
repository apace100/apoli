package io.github.apace100.apoli.power.factory.action.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;

public class TameAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {

        if (!(actorAndTarget.getRight() instanceof TameableEntity tameableTarget) || !(actorAndTarget.getLeft() instanceof PlayerEntity actorPlayer)) {
            return;
        }

        if (!tameableTarget.isTamed()) {
            tameableTarget.setOwner(actorPlayer);
        }

    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("tame"),
            new SerializableData(),
            TameAction::action
        );
    }

}
