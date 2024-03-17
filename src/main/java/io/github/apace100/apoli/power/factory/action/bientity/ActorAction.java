package io.github.apace100.apoli.power.factory.action.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Consumer;

public class ActorAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {

        Entity actor = actorAndTarget.getLeft();

        if (actor != null) {
            data.<Consumer<Entity>>get("action").accept(actor);
        }

    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("actor_action"),
            new SerializableData()
                .add("action", ApoliDataTypes.ENTITY_ACTION),
            ActorAction::action
        );
    }

}
