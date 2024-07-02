package io.github.apace100.apoli.power.factory.action.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.LeashableEntity;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Pair;

public class LeashAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {

        Entity actor = actorAndTarget.getLeft();
        Entity target = actorAndTarget.getRight();

        if (actor == null || !(target instanceof MobEntity mobTarget) || !(mobTarget instanceof LeashableEntity leashable)) {
            return;
        }

        if (!mobTarget.isLeashed()) {
            leashable.apoli$setCustomLeashed(true);
            mobTarget.attachLeash(actor, true);
        }

    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("leash"),
            new SerializableData(),
            LeashAction::action
        );
    }

}
