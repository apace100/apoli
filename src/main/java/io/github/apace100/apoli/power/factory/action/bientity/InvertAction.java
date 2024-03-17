package io.github.apace100.apoli.power.factory.action.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Consumer;

public class InvertAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {
        data.<Consumer<Pair<Entity, Entity>>>get("action").accept(new Pair<>(actorAndTarget.getRight(), actorAndTarget.getLeft()));
    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("invert"),
            new SerializableData()
                .add("action", ApoliDataTypes.BIENTITY_ACTION),
            InvertAction::action
        );
    }

}
