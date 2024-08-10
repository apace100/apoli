package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Consumer;

public class InvertActionType {

    public static void action(Entity actor, Entity target, Consumer<Pair<Entity, Entity>> biEntityAction) {
        biEntityAction.accept(new Pair<>(target, actor));
    }

    public static ActionTypeFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("invert"),
            new SerializableData()
                .add("action", ApoliDataTypes.BIENTITY_ACTION),
            (data, actorAndTarget) -> action(actorAndTarget.getLeft(), actorAndTarget.getRight(),
                data.get("action")
            )
        );
    }

}
