package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.EntitySetPowerType;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

public class RemoveFromEntitySetActionType {

    public static void action(Entity actor, Entity target, PowerReference power) {

        if (power.getType(actor) instanceof EntitySetPowerType entitySet && entitySet.remove(target)) {
            PowerHolderComponent.syncPower(actor, power);
        }

    }

    public static ActionTypeFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("remove_from_entity_set"),
            new SerializableData()
                .add("set", ApoliDataTypes.POWER_REFERENCE),
            (data, actorAndTarget) -> action(actorAndTarget.getLeft(), actorAndTarget.getRight(),
                data.get("set")
            )
        );
    }

}
