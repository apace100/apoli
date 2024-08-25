package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Consumer;

public class TargetActionType {

    public static void action(Entity target, Consumer<Entity> action) {

        if (target != null) {
            action.accept(target);
        }

    }

    public static ActionTypeFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("target_action"),
            new SerializableData()
                .add("action", ApoliDataTypes.ENTITY_ACTION),
            (data, actorAndTarget) -> action(actorAndTarget.getRight(),
                data.get("action")
            )
        );
    }

}
