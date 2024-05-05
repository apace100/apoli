package io.github.apace100.apoli.power.factory.action.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.EntitySetPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.IdentifierAlias;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

public class AddToEntitySetAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {

        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(actorAndTarget.getLeft());
        PowerType<?> powerType = data.get("set");

        if (component == null || powerType == null || !(component.getPower(powerType) instanceof EntitySetPower entitySetPower)) {
            return;
        }

        if (entitySetPower.add(actorAndTarget.getRight(), data.get("time_limit"))) {
            PowerHolderComponent.syncPower(actorAndTarget.getLeft(), powerType);
        }

    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        IdentifierAlias.addPathAlias("add_to_set", "add_to_entity_set");
        return new ActionFactory<>(
            Apoli.identifier("add_to_entity_set"),
            new SerializableData()
                .add("set", ApoliDataTypes.POWER_TYPE)
                .add("time_limit", SerializableDataTypes.POSITIVE_INT, null),
            AddToEntitySetAction::action
        );
    }
}
