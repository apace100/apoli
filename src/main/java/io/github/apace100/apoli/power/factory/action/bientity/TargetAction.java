package io.github.apace100.apoli.power.factory.action.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Consumer;

public class TargetAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {

        Entity target = actorAndTarget.getRight();

        if (target != null) {
            data.<Consumer<Entity>>get("action").accept(target);
        }

    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("target_action"),
            new SerializableData()
                .add("action", ApoliDataTypes.ENTITY_ACTION),
            TargetAction::action
        );
    }

}
