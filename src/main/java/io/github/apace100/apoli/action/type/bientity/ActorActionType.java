package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Consumer;

public class ActorActionType {

    public static void action(Entity actor, Consumer<Entity> action) {

        if (actor != null) {
            action.accept(actor);
        }

    }

    public static ActionTypeFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("actor_action"),
            new SerializableData()
                .add("action", ApoliDataTypes.ENTITY_ACTION),
            (data, actorAndTarget) -> action(actorAndTarget.getLeft(),
                data.get("action")
            )
        );
    }

}
