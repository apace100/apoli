package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.EntitySetPowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

public class AddToEntitySetActionType {

    public static void action(Entity actor, Entity target, PowerReference power, @Nullable Integer timeLimit) {

        if (power.getType(actor) instanceof EntitySetPowerType entitySet && entitySet.add(target, timeLimit)) {
            PowerHolderComponent.syncPower(actor, power);
        }

    }

    public static ActionTypeFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("add_to_entity_set"),
            new SerializableData()
                .add("set", ApoliDataTypes.POWER_REFERENCE)
                .add("time_limit", SerializableDataTypes.POSITIVE_INT, null),
            (data, actorAndTarget) -> action(actorAndTarget.getLeft(), actorAndTarget.getRight(),
                data.get("set"),
                data.get("time_limit")
            )
        );
    }

}
