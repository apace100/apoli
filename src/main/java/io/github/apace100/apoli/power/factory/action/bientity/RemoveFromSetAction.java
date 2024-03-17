package io.github.apace100.apoli.power.factory.action.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.EntitySetPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

public class RemoveFromSetAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {

        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(actorAndTarget.getLeft());
        PowerType<?> powerType = data.get("set");

        if (component == null || powerType == null || !(component.getPower(powerType) instanceof EntitySetPower entitySetPower)) {
            return;
        }

        if (entitySetPower.remove(actorAndTarget.getRight())) {
            PowerHolderComponent.syncPower(actorAndTarget.getLeft(), powerType);
        }

    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("remove_from_set"),
            new SerializableData()
                .add("set", ApoliDataTypes.POWER_TYPE),
            RemoveFromSetAction::action
        );
    }
}
